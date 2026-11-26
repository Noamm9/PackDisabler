package com.github.noamm9.packdisabler

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.*
import net.fabricmc.loader.api.*
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.*
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import java.io.InputStream
import java.net.URI
import java.net.http.*
import java.nio.file.*
import java.security.MessageDigest
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import kotlin.io.path.*

/**
 * @see com.github.noamm9.packdisabler.mixin.MixinMinecraft
 */
class HypixelPackLoader: LanguageAdapter {
    companion object {
        lateinit var INSTANCE: HypixelPackLoader
        const val packId = "hypixel_skyblock"
    }

    private val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    private val packDir = FabricLoader.getInstance().configDir.resolve("@MODID@").also(Files::createDirectories)
    @Volatile private var activePath: Path? = null
    private var reloadJob: Job? = null

    @Volatile private var pack: Pack? = null
    val activePack get() = pack ?: buildPack(runBlocking { initialPack.await() }).also { pack = it }
    val initialPack = PackDisabler.scope.async(start = CoroutineStart.LAZY) {
        val path = try {
            val (url, hash) = fetchPackInfo()
            download(url, hash)
        }
        catch (e: Exception) {
            PackDisabler.logger.error("Failed to download Hypixel pack", e)
            packDir.listDirectoryEntries("*.zip").maxByOrNull { it.getLastModifiedTime() } ?: extractBundled(packDir.resolve("bundled.zip"))
        }

        packDir.listDirectoryEntries("*.{zip,tmp}").filter { it != path }.forEach {
            runCatching { it.deleteIfExists() }
                .onFailure { e -> PackDisabler.logger.warn("Failed to delete old Hypixel pack file ${it.name}", e) }
                .onSuccess { _ -> PackDisabler.logger.info("Deleted old Hypixel pack file ${it.name}") }
        }

        PackDisabler.logger.info("Using Hypixel pack ${path.name}")
        activePath = path
        path
    }

    init {
        INSTANCE = this
        initialPack.start()
    }

    fun updatePack(url: String, hash: String) {
        if (activePath == packDir.resolve("${hash.lowercase()}.zip")) return PackDisabler.logger.info("Server sent the active Hypixel pack, skipping reload")
        PackDisabler.logger.info("Server sent a new Hypixel pack $hash")
        reload(url to hash.lowercase())
    }

    fun reload(packInfo: Pair<String, String>?, onComplete: (Boolean) -> Unit = {}) {
        if (reloadJob?.isActive == true) return PackDisabler.logger.info("Hypixel pack reload already in progress")
        PackDisabler.logger.info("Reloading Hypixel pack")
        val mc = Minecraft.getInstance()

        reloadJob = PackDisabler.scope.launch {
            val (url, hash) = packInfo ?: fetchPackInfo()
            val path = download(url, hash)
            pack = buildPack(path)
            activePath = path
            PackDisabler.logger.info("Reloading resources with Hypixel pack ${path.name}")
            mc.submit<CompletableFuture<Void>> { mc.reloadResourcePacks() }.thenCompose { it }.await()
        }.apply {
            invokeOnCompletion { error ->
                if (error != null) PackDisabler.logger.error("Failed to reload the Hypixel pack", error)
                else PackDisabler.logger.info("Hypixel pack reloaded")
                mc.execute { onComplete(error == null) }
                reloadJob = null
            }
        }
    }

    private fun fetchPackInfo(): Pair<String, String> {
        val response = "https://api.hypixel.net/v2/resources/packs".fetch()
        val packs = Json.parseToJsonElement(response.body().decodeToString()).jsonObject.getValue("packs").jsonArray.map { it.jsonObject }
        val versions = packs.first { it["id"]?.jsonPrimitive?.content == "SkyBlock" }.getValue("versions").jsonArray.map { it.jsonObject }
        val version = versions.find { it["packFormat"]?.jsonPrimitive?.content == "@PACKFORMAT@" } ?: error("No SkyBlock pack with format @PACKFORMAT@, available: ${versions.map { it["packFormat"] }}")
        val url = version.getValue("url").jsonPrimitive.content
        val hash = version.getValue("hash").jsonPrimitive.content.lowercase()
        return url to hash
    }

    private fun extractBundled(file: Path): Path {
        PackDisabler.logger.info("Extracting bundled fallback pack")
        val input = javaClass.getResourceAsStream("/pack_fallback.zip") ?: error("Bundled fallback pack not found in jar")
        return write(input, file)
    }

    private fun download(url: String, hash: String): Path {
        val file = packDir.resolve("$hash.zip")
        if (file.exists()) {
            PackDisabler.logger.info("Hypixel pack ${file.name} already downloaded")
            return file
        }

        val startTime = System.currentTimeMillis()
        val response = url.fetch()
        check(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(response.body())) == hash) { "Does not match hash $hash" }
        write(response.body().inputStream(), file)
        PackDisabler.logger.info("Download finished in ${System.currentTimeMillis() - startTime}ms (${response.body().size / 1024 / 1024} MB)")
        return file
    }

    private fun write(source: InputStream, file: Path) = source.use {
        val tmp = file.resolveSibling("${file.name}.tmp")
        Files.copy(it, tmp, StandardCopyOption.REPLACE_EXISTING)
        tmp.moveTo(file, overwrite = true)
    }

    private fun buildPack(packPath: Path): Pack {
        val locationInfo = PackLocationInfo(
            packId,
            Component.literal("PackDisabler: SkyblockPack"),
            PackSource.BUILT_IN,
            Optional.empty()
        )

        val selectionConfig = PackSelectionConfig(
            true, // required
            Pack.Position.BOTTOM,
            true // fixedPosition
        )

        val resourcesSupplier = FilePackResources.FileResourcesSupplier(packPath.toFile())

        return Pack.readMetaAndCreate(
            locationInfo,
            resourcesSupplier,
            PackType.CLIENT_RESOURCES,
            selectionConfig
        ) ?: error("Failed to read pack metadata for $packPath")
    }

    private fun String.fetch(): HttpResponse<ByteArray> {
        val r = HttpRequest.newBuilder().apply {
            PackDisabler.logger.info("Fetching ${this@fetch}")
            header("User-Agent", "Minecraft Java")
            version(HttpClient.Version.HTTP_1_1)
            timeout(Duration.ofSeconds(10))
            uri(URI.create(this@fetch))
            GET()
        }.build()

        return httpClient.send(r, HttpResponse.BodyHandlers.ofByteArray()).apply {
            check(statusCode() in 200 .. 299) { "Returned status ${statusCode()}" }
        }
    }

    override fun <T> create(mod: ModContainer?, value: String?, type: Class<T?>?) = error("Not implemented")
}