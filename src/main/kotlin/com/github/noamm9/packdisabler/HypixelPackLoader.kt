package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.PackDisabler.Companion.httpClient
import com.github.noamm9.packdisabler.PackDisabler.Companion.logger
import com.github.noamm9.packdisabler.config.Config
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
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.function.*
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object HypixelPackLoader {
    private const val packUrl = "https://resourcepacks.hypixel.net/SkyBlock/5c59e0a9-9865-4d4e-91d2-915515672cbd/84.zip"
    private val packDir = FabricLoader.getInstance().configDir.resolve("@MODID@")
    private val packFile = packDir.resolve("hypixel_skyblock.zip")
    private val etagFile = packDir.resolve("hypixel_skyblock.zip.etag")

    private lateinit var activePack: Pack

    fun init() = runCatching {
        Files.createDirectories(packDir)
        downloadPack()
        activePack = buildPack()

        logger.info("binding hypixel texturepack")
        Minecraft.getInstance().apply { execute(resourcePackRepository::reload) }

    }.onFailure {
        logger.error("Failed to load Hypixel pack", it)
    }

    private fun downloadPack() {
        logger.info("Downloading hypixel pack...")
        val url = Config.get("packUrl") ?: packUrl
        val storedEtag = etagFile.takeIf(Path::exists)?.readText()

        val head = HttpRequest.newBuilder(URI.create(url)).method("HEAD", HttpRequest.BodyPublishers.noBody()).build()
        val headResp = runCatching { httpClient.send(head, HttpResponse.BodyHandlers.discarding()) }
            .onFailure { logger.error("Failed to reach $url for HEAD request", it) }
            .getOrNull() ?: return

        if (headResp.statusCode() !in 200 .. 299) {
            logger.error("HEAD request to $url returned status ${headResp.statusCode()}")
            return
        }

        val remoteEtag = headResp.headers().firstValue("etag").orElse(null)

        if (remoteEtag != null && remoteEtag == storedEtag && packFile.exists()) return

        val tmp = Files.createTempFile(packDir, "hypixel_pack", ".tmp")
        val get = HttpRequest.newBuilder(URI.create(url)).header("Accept-Encoding", "gzip").build()
        val getResp = runCatching { httpClient.send(get, HttpResponse.BodyHandlers.ofFile(tmp)) }
            .onFailure { logger.error("Failed to download pack from $url", it) }
            .getOrNull() ?: return

        if (getResp.statusCode() !in 200 .. 299) {
            logger.error("GET request to $url returned status ${getResp.statusCode()}, discarding")
            Files.deleteIfExists(tmp)
            return
        }

        Files.move(tmp, packFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        remoteEtag?.let(etagFile::writeText)
        logger.info("Hypixel pack downloaded successfully")
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
        override fun loadPacks(onLoad: Consumer<Pack>) {
            onLoad.accept(activePack)
        }
    }
}