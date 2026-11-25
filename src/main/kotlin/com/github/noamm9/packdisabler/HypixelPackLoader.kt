package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.config.Config
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.fabricmc.loader.api.FabricLoader
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
 * @see PackDisablerPreLaunch
 * @see com.github.noamm9.packdisabler.mixin.MixinMinecraft
 */
object HypixelPackLoader {
    const val packId = "hypixel_skyblock"

    private val packDir = FabricLoader.getInstance().configDir.resolve("@MODID@")
    private val httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()
    private var reloadJob: Job? = null

    @Volatile var activePack = lazy { buildPack(runBlocking { initialPack.await() }) }
    val initialPack = PackDisabler.scope.async(start = CoroutineStart.LAZY) {
        val path = preparePack()
        packDir.listDirectoryEntries("*.{zip,sha1,tmp}").filter { it != path }.forEach { runCatching { it.deleteIfExists() } }
        path
    }

    fun reload(onComplete: (Boolean) -> Unit = {}) {
        if (reloadJob?.isActive == true) return
        val mc = Minecraft.getInstance()

        reloadJob = PackDisabler.scope.launch {
            activePack = lazyOf(buildPack(preparePack(allowFallback = false)))
            mc.submit<CompletableFuture<Void>> { mc.reloadResourcePacks() }.thenCompose { it }.await()
        }.apply {
            invokeOnCompletion { error ->
                if (error != null) PackDisabler.logger.error("Failed to reload the Hypixel pack", error)
                mc.execute { onComplete(error == null) }
                reloadJob = null
            }
        }
    }

    fun updatePack(url: String, hash: String) {
        if (Config.packUrl == url && Config.packHash == hash.lowercase().ifEmpty { null }) return
        Config.packUrl = url
        Config.packHash = hash.lowercase()
        reload()
    }

    // the bundled pack has no sounds, so it never takes the hash name of a real download
    private fun preparePack(allowFallback: Boolean = true): Path {
        Files.createDirectories(packDir)
        val hash = Config.packHash
        val url = Config.packUrl
        if (hash != null && url != null) {
            val file = packDir.resolve("$hash.zip")
            if (file.exists() || download(url, hash, file)) return file
            check(allowFallback) { "No valid Hypixel pack was downloaded" }
        }

        return packDir.listDirectoryEntries("*.zip").maxByOrNull { it.getLastModifiedTime() } ?: extractBundled(packDir.resolve("bundled.zip"))
    }

    private fun extractBundled(file: Path): Path {
        PackDisabler.logger.info("Extracting bundled fallback pack")
        val input = javaClass.getResourceAsStream("/pack_fallback.zip") ?: error("Bundled fallback pack not found in jar")
        input.use { write(it, file) }
        return file
    }

    private fun download(url: String, hash: String, file: Path) = runCatching {
        PackDisabler.logger.info("Downloading hypixel pack from $url")
        val request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray())
        check(response.statusCode() in 200 .. 299) { "Returned status ${response.statusCode()}" }
        check(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(response.body())) == hash) { "Does not match hash $hash" }
        write(response.body().inputStream(), file)
    }.onFailure { PackDisabler.logger.error("Failed to download Hypixel pack from $url", it) }.isSuccess

    private fun write(source: InputStream, file: Path) {
        val tmp = file.resolveSibling("${file.name}.tmp")
        Files.copy(source, tmp, StandardCopyOption.REPLACE_EXISTING)
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
}