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

        private val cacheFile = YACLPlatform.getConfigDir().resolve("@MODID@").resolve("cache").toFile()
        private val TTL_MS = TimeUnit.DAYS.toMillis(3)
    }

    override fun onInitializeClient() {
        Config.handler.load()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(literal("@MODID@").executes {
                val mc = Minecraft.getInstance()
                mc.execute {
                    //? <26.2 {
                    /*mc.setScreen(Config.createScreen(null))
                    *///?} else {
                    mc.gui.setScreen(Config.createScreen(null))
                    //?}
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
        if (cacheFile.exists() && System.currentTimeMillis() - cacheFile.lastModified() < TTL_MS) {
            logger.info("Loading Skyblock items from cache")
            return cacheFile.readText()
        }

        logger.info("Fetching Skyblock items from API")
        val url = URI.create("https://api.noamm.org/resources/skyblock-items").toURL()
        val connection = url.openConnection() as HttpsURLConnection
        connection.setRequestProperty("User-Agent", this::class.simpleName)
        connection.requestMethod = "GET"

        return connection.inputStream.bufferedReader().readText().also(cacheFile::writeText)
    }

    private fun createProfile(sbid: String, texture: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))
        val profile = GameProfile(UUID.nameUUIDFromBytes("@MODID@:$sbid".toByteArray()), this::class.simpleName, properties)
        return ResolvableProfile.createResolved(profile)
    }
}