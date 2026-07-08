package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.config.Config
import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.brigadier.Command
import dev.isxander.yacl3.platform.YACLPlatform
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import java.util.concurrent.*
import javax.net.ssl.HttpsURLConnection
import net.fabricmc.loader.api.FabricLoader
import java.io.File

//? =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal
//?}

@Entrypoint(Entrypoint.CLIENT)
class PackDisabler: ClientModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger(PackDisabler::class.java)
        var idToLocation = HashMap<String, Identifier>()
        val idToSkullProfile = HashMap<String, ResolvableProfile>()

        val cacheDir = YACLPlatform.getConfigDir().resolve("@MODID@").resolve("cache")
        private val cacheFile = cacheDir.resolve("cache").toFile()
        private val versionFile = cacheDir.resolve("version").toFile()
        private val cacheTTL = TimeUnit.DAYS.toMillis(1)

        private val version = FabricLoader.getInstance().getModContainer("@MODID@").get().metadata.version.friendlyString
    }

    override fun onInitializeClient() {
        Config.handler.load()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(literal("@MODID@").executes {
                val mc = Minecraft.getInstance()
                mc.execute {
                    //? <26.2 {
                    mc.setScreen(Config.createScreen(null))
                    //?} else {
                    /*mc.gui.setScreen(Config.createScreen(null))
                    *///?}
                }
                Command.SINGLE_SUCCESS
            })
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for ((sbid, element) in Json.parseToJsonElement(getData()).jsonObject) {
                    val item = element.jsonObject
                    val model = item["model"]?.jsonPrimitive?.content ?: continue
                    val texture = item["texture"]?.jsonPrimitive?.content

                    idToLocation[sbid] = Identifier.parse(model)
                    if (! texture.isNullOrEmpty()) idToSkullProfile[sbid] = createProfile(sbid, texture)
                }

                logger.info("Finished loading ${idToLocation.size} items")
            }
            catch (e: Exception) {
                logger.error("Failed to load Skyblock items", e)
            }
        }
    }

    private fun getData(): String {
        cacheDir.toFile().mkdirs()

        val cachedVersion = versionFile.takeIf(File::exists)?.readText()
        val versionMatches = cachedVersion == version
        val notExpired = cacheFile.exists() && System.currentTimeMillis() - cacheFile.lastModified() < cacheTTL

        if (versionMatches && notExpired) {
            logger.info("Loading Skyblock items from cache")
            return cacheFile.readText()
        }

        if (!versionMatches && cacheFile.exists()) {
            logger.info("Mod version changed. invalidating cache")
            cacheFile.delete()
        }

        logger.info("Fetching Skyblock items from API")
        val url = URI.create("https://api.noamm.org/resources/skyblock-items").toURL()
        val connection = url.openConnection() as HttpsURLConnection
        connection.setRequestProperty("User-Agent", this::class.simpleName)
        connection.requestMethod = "GET"

        return connection.inputStream.bufferedReader().readText().also {
            versionFile.writeText(version)
            cacheFile.writeText(it)
        }
    }

    private fun createProfile(sbid: String, texture: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))
        val profile = GameProfile(UUID.nameUUIDFromBytes("@MODID@:$sbid".toByteArray()), this::class.simpleName, properties)
        return ResolvableProfile.createResolved(profile)
    }
}