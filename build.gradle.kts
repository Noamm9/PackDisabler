import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.blossom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingtable.fabric)
    id("net.fabricmc.fabric-loom")
}

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
    val kofi = property("mod.kofi")
}

class Dependencies {
    val fabricLoaderVersion = property("deps.fabric_loader_version")
    val fabricKotlinVersion = property("deps.fabric_kotlin_version")
    val fabricApiVersion = property("deps.fabric_api_version")
    val devauthVersion = property("deps.devauth_version")
}

val mod = ModData()
val deps = Dependencies()
val mcVersion = stonecutter.current.version

version = "${mod.version}+${mcVersion}"
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
}

dependencies {
    "minecraft"("com.mojang:minecraft:$mcVersion")

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:${deps.devauthVersion}")
    implementation("net.fabricmc:fabric-loader:${deps.fabricLoaderVersion}")
    implementation("net.fabricmc:fabric-language-kotlin:${deps.fabricKotlinVersion}")
    implementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApiVersion}+$mcVersion") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-content-registries-v0")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin { jvmToolchain(25) }

tasks.processResources {
    val props = buildMap {
        put("id", mod.id)
        put("name", mod.name)
        put("version", mod.version)
        put("mcdep", mcVersion)
        put("description", mod.description)
        put("source", mod.source)
        put("issues", mod.issues)
        put("license", mod.license)
        put("modrinth", mod.modrinth)
        put("discord", mod.discord)
        put("kofi", mod.kofi)
        put("fabric_loader_version", deps.fabricLoaderVersion)
    }

    props.forEach(inputs::property)

    filesMatching("fabric.mod.json") { expand(props) }
}