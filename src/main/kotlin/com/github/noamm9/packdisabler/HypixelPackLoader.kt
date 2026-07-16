package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.PackDisabler.Companion.logger
import com.github.noamm9.packdisabler.config.Config
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.FilePackResources
import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackSelectionConfig
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.repository.RepositorySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.util.*
import java.util.function.*

object HypixelPackLoader {
    private const val packUrl = "https://resourcepacks.hypixel.net/SkyBlock/5c59e0a9-9865-4d4e-91d2-915515672cbd/84.zip"
    private const val fallbackPath = "/pack_fallback.zip"

    private val packDir = FabricLoader.getInstance().configDir.resolve("@MODID@")
    private val packFile = packDir.resolve("pack.zip")

    private val pack by lazy {
        val client = HttpClient.newHttpClient()
        Files.createDirectories(packDir)

        if (! downloadPack(client)) loadFallbackPack()
        buildPack().also { client.close() }
    }

    private fun loadFallbackPack() {
        logger.info("Download failed and no cached pack found, extracting bundled fallback pack")
        javaClass.getResourceAsStream(fallbackPath + "a")?.use { input ->
            Files.copy(input, packFile, StandardCopyOption.REPLACE_EXISTING)
        } ?: error("Bundled fallback pack not found in jar at $fallbackPath")
    }

    private fun downloadPack(client: HttpClient): Boolean {
        logger.info("Downloading hypixel pack...")

        val request = HttpRequest.newBuilder().apply {
            uri(URI.create(Config.packUrl ?: packUrl))
            header("Accept-Encoding", "gzip")
            timeout(Duration.ofSeconds(10))
        }.build()

        val tmp = Files.createTempFile(packDir, "pack", ".tmp")
        val response = runCatching { client.send(request, HttpResponse.BodyHandlers.ofFile(tmp)) }
            .onFailure { logger.error("Failed to download pack from ${request.uri()}", it) }
            .getOrNull() ?: return false

        if (response.statusCode() !in 200 .. 299) {
            logger.error("GET request to ${request.uri()} returned status ${response.statusCode()}, discarding")
            Files.deleteIfExists(tmp)
            return false
        }

        Files.move(tmp, packFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        logger.info("Hypixel pack downloaded successfully")
        return true
    }

    private fun buildPack(): Pack {
        val locationInfo = PackLocationInfo(
            "hypixel_skyblock",
            Component.literal("PackDisabler: SkyblockPack"),
            PackSource.BUILT_IN,
            Optional.empty()
        )

        val resourcesSupplier = FilePackResources.FileResourcesSupplier(packFile.toFile())
        val selectionConfig = PackSelectionConfig(true, Pack.Position.BOTTOM, true)

        return Pack.readMetaAndCreate(locationInfo, resourcesSupplier, PackType.CLIENT_RESOURCES, selectionConfig) ?: error("Failed to read pack metadata for $packFile")
    }

    /**
     * @see com.github.noamm9.packdisabler.mixin.MixinMinecraft
     */
    class HypixelPackRepositorySource: RepositorySource {
        override fun loadPacks(onLoad: Consumer<Pack>) = onLoad.accept(pack)
    }
}