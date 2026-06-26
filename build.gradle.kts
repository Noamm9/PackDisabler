import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.blossom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingtable.fabric)
    id("net.fabricmc.fabric-loom") apply false
    id("net.fabricmc.fabric-loom-remap") apply false
}

val obfuscated = property("mod.mc_version").toString().let { version ->
    !version.startsWith("26.")
}
plugins.apply(if (obfuscated) "net.fabricmc.fabric-loom-remap" else "net.fabricmc.fabric-loom")

val loom = the<LoomGradleExtensionAPI>()
val modImplementation = if (obfuscated) configurations.named("modImplementation") else configurations.implementation
val modRuntimeOnly = if (obfuscated) configurations.named("modRuntimeOnly") else configurations.runtimeOnly

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name")
    val version = property("mod.version")
    val group = property("mod.group").toString()
    val description = property("mod.description")
    val source = property("mod.source")
    val issues = property("mod.issues")
    val license = property("mod.license").toString()
    val modrinth = property("mod.modrinth")
    val curseforge = property("mod.curseforge")
    val kofi = property("mod.kofi")
    val discord = property("mod.discord")
}

class Dependencies {
    val fabricLoaderVersion = property("deps.fabric_loader_version")
    val fabricApiVersion = property("deps.fabric_api_version")
    val devauthVersion = property("deps.devauth_version")
    val mixinconstraintsVersion = property("deps.mixinconstraints_version")
    val mixinsquaredVersion = property("deps.mixinsquared_version")
}

class McData {
    val version = property("mod.mc_version")
    val dep = property("mod.mc_dep").toString()
}

val mc = McData()
val mod = ModData()
val deps = Dependencies()
val loader = "fabric"

version = "${mod.version}+${mc.version}-$loader"
group = mod.group
base { archivesName.set(mod.id) }

stonecutter {
    constants["fabric"] = true
}

blossom {
    replaceToken("@MODID@", mod.id)
}

extensions.configure<LoomGradleExtensionAPI> {
    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run" // This sets the run folder for all mc versions to the same folder. Remove this line if you want individual run folders.
    }

    runConfigs.remove(runConfigs["server"]) // Removes server run configs

    runs {
        afterEvaluate {
            val mixinJarFile = configurations.runtimeClasspath.get().incoming.artifactView {
                componentFilter {
                    it is ModuleComponentIdentifier && it.group == "net.fabricmc" && it.module == "sponge-mixin"
                }
            }.files.firstOrNull()

            configureEach {
                if (mixinJarFile != null) vmArg("-javaagent:$mixinJarFile")
                vmArg("-XX:+AllowEnhancedClassRedefinition")

                property("mixin.hotSwap", "true")
                property("mixin.debug.export", "true") // Puts mixin outputs in /run/.mixin.out
            }
        }
    }
}

fletchingTable {
    mixins.create("main") {
        mixin("default", "${mod.id}.mixins.json")
    }

    lang.create("main") {
        patterns.add("assets/${mod.id}/lang/**")
    }
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") // DevAuth
    maven("https://maven.bawnorton.com/releases") // MixinSquared
    maven("https://api.modrinth.com/maven") // Modrinth
}

dependencies {
    "minecraft"("com.mojang:minecraft:${mc.version}")
    if (obfuscated) {
        "mappings"(loom.officialMojangMappings())
    }

    modRuntimeOnly("me.djtheredstoner:DevAuth-$loader:${deps.devauthVersion}")
    modImplementation("net.fabricmc:fabric-loader:${deps.fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApiVersion}+${mc.version}") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-content-registries-v0")
    }

    val mixinconstraints = implementation("com.moulberry:mixinconstraints:${deps.mixinconstraintsVersion}")!!
    val mixinsquared = implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-$loader:${deps.mixinsquaredVersion}")!!)!!
    add("include", mixinconstraints)
    add("include", mixinsquared)
}

java {
    // withSourcesJar() // Uncomment if you want sources
    val javaVersion = if (obfuscated) JavaVersion.VERSION_21 else JavaVersion.VERSION_25
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.processResources {
    val props = buildMap {
        put("id", mod.id)
        put("name", mod.name)
        put("version", mod.version)
        put("mcdep", mc.dep)
        put("description", mod.description)
        put("source", mod.source)
        put("issues", mod.issues)
        put("license", mod.license)
        put("modrinth", mod.modrinth)
        put("curseforge", mod.curseforge)
        put("kofi", mod.kofi)
        put("discord", mod.discord)
        put("fabric_loader_version", deps.fabricLoaderVersion)
    }

    props.forEach(inputs::property)

    filesMatching("**/lang/en_us.json") { // Defaults description to English translation
        expand(props)
        filteringCharset = "UTF-8"
    }

    filesMatching("fabric.mod.json") { expand(props) }
}

if (stonecutter.current.isActive) {
    rootProject.tasks.register("buildActive") {
        group = "project"
        dependsOn(tasks.named("build"))
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?): T? =
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)
