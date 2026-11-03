import groovy.json.JsonSlurper
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import java.net.URI
import java.net.http.*
import java.security.MessageDigest
import java.util.zip.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.blossom)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fletchingtable.fabric)
    id("net.fabricmc.fabric-loom")
}

val loom = the<LoomGradleExtensionAPI>()
val modImplementation = configurations.implementation
val modRuntimeOnly = configurations.runtimeOnly

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
    val packFormat = property("mod.pack_format").toString().toInt()
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
    replaceToken("@VERSION@", mod.version)
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
                if (mixinJarFile != null) jvmArguments.add("-javaagent:$mixinJarFile")
                jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")

                if (stonecutter.eval(mcVersion, ">=26.3")) {
                    jvmArguments.add("java.base/jdk.internal.misc=ALL-UNNAMED")
                    jvmArguments.add("-XX:StackShadowPages=32")
                    jvmArguments.add("--add-exports")
                }

                systemProperties.put("mixin.hotSwap", "true")
                systemProperties.put("mixin.debug.export", "true")
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
    maven("https://api.modrinth.com/maven") // Modrinth
}

dependencies {
    "minecraft"("com.mojang:minecraft:$mcVersion")

    modRuntimeOnly("me.djtheredstoner:DevAuth-fabric:${deps.devauthVersion}")
    modImplementation("net.fabricmc:fabric-loader:${deps.fabricLoaderVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${deps.fabricKotlinVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps.fabricApiVersion}+$mcVersion") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-content-registries-v0")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin { jvmToolchain(25) }

val downloadHypixelPack by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/hypixelPack")
    val hashFile = layout.buildDirectory.file("generated/hypixelPack.sha1")

    inputs.property("packFormat", mod.packFormat)
    outputs.dir(outputDir)
    outputs.file(hashFile)
    outputs.upToDateWhen { false }

    doLast {
        val zipFile = outputDir.get().file("pack_fallback.zip").asFile
        val urlFile = outputDir.get().file("pack_fallback.url").asFile
        val hashFile = hashFile.get().asFile
        val httpClient = HttpClient.newHttpClient()
        fun sha1(bytes: ByteArray) = MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }

        @Suppress("UNCHECKED_CAST")
        val version = runCatching {
            val request = HttpRequest.newBuilder(URI.create("https://api.hypixel.net/v2/resources/packs")).build()
            val body = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body()

            val packs = (JsonSlurper().parseText(body) as Map<String, Any>)["packs"] as List<Map<String, Any>>
            val versions = packs.first { it["id"] == "SkyBlock" }["versions"] as List<Map<String, Any>>
            versions.firstOrNull { it["packFormat"] == mod.packFormat } ?: error("No SkyBlock pack with format ${mod.packFormat}, available: ${versions.map { it["packFormat"] }}")
        }.getOrElse {
            if (! zipFile.exists() || ! urlFile.exists()) throw GradleException("Failed to fetch Hypixel pack info", it)
            logger.warn("Failed to fetch Hypixel pack info, reusing previously downloaded pack: ${it.message}")
            return@doLast
        }

        val url = version["url"].toString()
        val hash = version["hash"].toString()

        if (! zipFile.exists() || ! hashFile.exists() || hashFile.readText() != hash) {
            logger.lifecycle("Downloading Hypixel pack from $url")
            val bytes = httpClient.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray()).body()
            if (sha1(bytes) != hash) throw GradleException("Hypixel pack hash mismatch for $url")
            zipFile.parentFile.mkdirs()

            // sounds are fucking 16MB wtf?
            ZipInputStream(bytes.inputStream()).use { input ->
                ZipOutputStream(zipFile.outputStream()).use { output ->
                    generateSequence { input.nextEntry }
                        .filterNot { it.name.startsWith("assets/hypixel_skyblock/sounds") }
                        .forEach { entry ->
                            output.putNextEntry(ZipEntry(entry.name))
                            input.copyTo(output)
                            output.closeEntry()
                        }
                }
            }

            hashFile.writeText(hash)
            logger.lifecycle("Download finished")
        }

        urlFile.writeText(url)
    }
}

tasks.processResources {
    from(downloadHypixelPack)

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

    filesMatching("**/lang/en_us.json") {
        expand(props)
        filteringCharset = "UTF-8"
    }

    filesMatching("fabric.mod.json") { expand(props) }
}