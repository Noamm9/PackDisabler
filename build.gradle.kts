import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.blossom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingtable.fabric)
    id("net.fabricmc.fabric-loom") apply false
    id("net.fabricmc.fabric-loom-remap") apply false
}

val obfuscated = property("mod.mc_version").toString().let { ! it.startsWith("26.") }
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
    val discord = property("mod.discord")
}

class Dependencies {
    val fabricLoaderVersion = property("deps.fabric_loader_version")
    val fabricKotlinVersion = property("deps.fabric_kotlin_version")
    val fabricApiVersion = property("deps.fabric_api_version")
    val yaclVersion = property("deps.yacl_version")
    val modmenuVersion = property("deps.modmenu_version")
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

version = "${mod.version}+${mc.version}"
group = mod.group
base { archivesName.set(mod.id) }
blossom {
    replaceToken("@MODID@", mod.id)
}

extensions.configure<LoomGradleExtensionAPI> {
    runConfigs.all { ideConfigGenerated(stonecutter.current.isActive) }
    runConfigs.remove(runConfigs["server"])

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
                property("mixin.debug.export", "true")
            }
        }
    }
}

fletchingTable {
    mixins.create("main") {
        mixin("default", "${mod.id}.mixins.json")
    }
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") // DevAuth
    maven("https://maven.bawnorton.com/releases") // MixinSquared
    maven("https://api.modrinth.com/maven") // Modrinth
    maven("https://maven.isxander.dev/releases") // YACL
    maven("https://maven.terraformersmc.com/") // ModMenu
}

dependencies {
    "minecraft"("com.mojang:minecraft:${mc.version}")
    if (obfuscated) "mappings"(loom.officialMojangMappings())

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${deps.devauthVersion}")
    modImplementation("net.fabricmc:fabric-loader:${deps.fabricLoaderVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${deps.fabricKotlinVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApiVersion}+${mc.version}") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-content-registries-v0")
    }

    modImplementation("com.terraformersmc:modmenu:${deps.modmenuVersion}")
    modImplementation("dev.isxander:yet-another-config-lib:${deps.yaclVersion}-fabric")

    val mixinconstraints = implementation("com.moulberry:mixinconstraints:${deps.mixinconstraintsVersion}") !!
    val mixinsquared = implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-fabric:${deps.mixinsquaredVersion}") !!) !!
    add("include", mixinconstraints)
    add("include", mixinsquared)
}



java {
    val javaVersion = if (obfuscated) JavaVersion.VERSION_21 else JavaVersion.VERSION_25
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

kotlin { jvmToolchain(if (obfuscated) 21 else 25) }

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
        put("discord", mod.discord)
        put("fabric_loader_version", deps.fabricLoaderVersion)
        put("yacl_version", deps.yaclVersion)
        put("modmenu_version", deps.modmenuVersion)
    }

    props.forEach(inputs::property)

    filesMatching("**/lang/en_us.json") {
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