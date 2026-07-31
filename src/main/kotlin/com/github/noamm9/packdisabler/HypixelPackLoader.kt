package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.PackDisabler.Companion.logger
import com.github.noamm9.packdisabler.config.Config
import kotlinx.coroutines.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackSelectionConfig
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.RepositorySource
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.*
import java.util.function.*
import kotlin.io.path.exists

/**
 * @see com.github.noamm9.packdisabler.mixin.MixinMinecraft
 */
object HypixelPackLoader {
    private const val packUrl = "https://resourcepacks.hypixel.net/SkyBlock/5c59e0a9-9865-4d4e-91d2-915515672cbd/84.zip"
    private const val fallbackPath = "/pack_fallback.zip"

    private val packDir = FabricLoader.getInstance().configDir.resolve("@MODID@")
    private val packFileA = packDir.resolve("pack-a.zip")
    private val packFileB = packDir.resolve("pack-b.zip")

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("PackDisabler-HypixelPackLoader"))
    private val httpClient = HttpClient.newHttpClient()
    private var reloadJob: Job? = null

    @Volatile private var activePackFile = getPackFile() ?: packFileA
    @Volatile private var activePack = lazy {
        runBlocking(scope.coroutineContext) {
            preparePack(activePackFile, allowCachedFallback = true) ?: error("Failed to prepare initial Hypixel pack")
        }
    }

    fun reload(onComplete: (Boolean) -> Unit = {}) {
        if (reloadJob?.isActive == true) return

        reloadJob = scope.launch {
            val targetFile = if (activePackFile == packFileA) packFileB else packFileA
            val pack = preparePack(targetFile) ?: error("No valid Hypixel pack was downloaded")
            activePack = lazyOf(pack)
            activePackFile = targetFile
            Minecraft.getInstance().reloadResourcePacks()
        }

        reloadJob?.invokeOnCompletion { error ->
            if (error != null) logger.error("Failed to reload the Hypixel pack", error)
            onComplete(error == null)
            reloadJob = null
        }
    }

    fun updatePackUrl(url: String) {
        if (Config.packUrl == url) return
        Config.packUrl = url
        reload()
    }

    private suspend fun preparePack(targetFile: Path, allowCachedFallback: Boolean = false): Pack? {
        withContext(Dispatchers.IO) { Files.createDirectories(packDir) }
        val path = downloadPack(targetFile) ?: (if (allowCachedFallback) getPackFile() ?: targetFile else return null)

        if (! path.exists()) {
            logger.info("Download failed, extracting bundled fallback pack")
            javaClass.getResourceAsStream(fallbackPath)?.use { input ->
                Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING)
            } ?: error("Bundled fallback pack not found in jar at $fallbackPath")
        }

        return buildPack(path)
    }

    private suspend fun downloadPack(targetFile: Path): Path? {
        logger.info("Downloading hypixel pack...")

        val request = HttpRequest.newBuilder().apply {
            uri(URI.create(Config.packUrl ?: packUrl))
            header("Accept-Encoding", "gzip")
            timeout(Duration.ofSeconds(10))
        }.build()

        val tmp = withContext(Dispatchers.IO) { Files.createTempFile(packDir, "pack", ".tmp") }
        val response = runCatching { withContext(Dispatchers.IO) { httpClient.send(request, HttpResponse.BodyHandlers.ofFile(tmp)) } }.onFailure {
            logger.error("Failed to download pack from ${request.uri()}", it)
            Files.deleteIfExists(tmp)
        }.getOrNull() ?: return null

        if (response.statusCode() !in 200 .. 299) {
            logger.error("GET request to ${request.uri()} returned status ${response.statusCode()}, discarding")
            withContext(Dispatchers.IO) { Files.deleteIfExists(tmp) }
            return null
        }

        return try {
            withContext(Dispatchers.IO) { Files.move(tmp, targetFile, StandardCopyOption.REPLACE_EXISTING) }
            logger.info("Hypixel pack downloaded successfully")
            targetFile
        }
        catch (error: FileSystemException) {
            if (! Files.isRegularFile(targetFile)) {
                withContext(Dispatchers.IO) { Files.deleteIfExists(tmp) }
                throw error
            }
            logger.warn("Cannot replace $targetFile; using fresh download for this session", error)
            tmp.toFile().deleteOnExit()
            tmp
        }
        catch (error: IOException) {
            logger.error("Failed to move downloaded pack into place", error)
            withContext(Dispatchers.IO) { Files.deleteIfExists(tmp) }
            throw error
        }
    }

    private fun buildPack(packPath: Path): Pack {
        val locationInfo = PackLocationInfo(
            "hypixel_skyblock",
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

    private fun getPackFile() = listOf(packFileA, packFileB).maxByOrNull(Files::getLastModifiedTime)

    class HypixelPackRepositorySource: RepositorySource {
        override fun loadPacks(onLoad: Consumer<Pack>) = onLoad.accept(activePack.value)
    }
}