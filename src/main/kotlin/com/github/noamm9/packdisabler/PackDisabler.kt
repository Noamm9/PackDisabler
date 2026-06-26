package com.github.noamm9.packdisabler

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.api.ClientModInitializer
import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Entrypoint(Entrypoint.CLIENT)
class PackDisabler : ClientModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("PackDisabler")

        @JvmField var idToLocation = ConcurrentHashMap<String, Identifier>()
        @JvmField val idToSkullProfile = ConcurrentHashMap<String, ResolvableProfile>()
    }

    override fun onInitializeClient() {
        logger.info("PackDisabler Initializing")

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = (URL("https://api.noamm.org/resources/skyblock-items").openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", this::class.simpleName)
                    }.inputStream.bufferedReader().readText()

                    for ((sbid, element) in Json.parseToJsonElement(response).jsonObject) {
                        val item = element.jsonObject
                        val id = item["id"]?.jsonPrimitive?.content ?: continue
                        val model = item["model"]?.jsonPrimitive?.content
                        val texture = item["texture"]?.jsonPrimitive?.content

                        idToLocation[sbid] = Identifier.parse(model ?: id)
                        if (!texture.isNullOrEmpty()) idToSkullProfile[sbid] = createSkullProfile(sbid, texture)
                    }

                    logger.info("PackDisabler finished loading ${idToLocation.size} items")
                } catch (e: Exception) {
                    logger.error("Failed to fetch Skyblock items", e)
                }
            }
        }
    }

    fun createSkullProfile(sbid: String, texture: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))
        val profile = GameProfile(UUID.nameUUIDFromBytes("@MODID@:$sbid".toByteArray()), this::class.simpleName, properties)
        return ResolvableProfile.createResolved(profile)
    }
}