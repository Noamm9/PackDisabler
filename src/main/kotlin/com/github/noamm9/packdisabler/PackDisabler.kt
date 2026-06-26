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
import java.net.URI
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.HttpsURLConnection

@Entrypoint(Entrypoint.CLIENT)
class PackDisabler : ClientModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger(PackDisabler::class.java)
        var idToLocation = ConcurrentHashMap<String, Identifier>()
        val idToSkullProfile = ConcurrentHashMap<String, ResolvableProfile>()
    }

    override fun onInitializeClient() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                try {
                    val response = (URI.create("https://api.noamm.org/resources/skyblock-items").toURL().openConnection() as HttpsURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", this::class.simpleName)
                    }.inputStream.bufferedReader().readText()

                    for ((sbid, element) in Json.parseToJsonElement(response).jsonObject) {
                        val item = element.jsonObject
                        val model = item["model"]?.jsonPrimitive?.content ?: continue
                        val texture = item["texture"]?.jsonPrimitive?.content

                        idToLocation[sbid] = Identifier.parse(model)
                        if (!texture.isNullOrEmpty()) idToSkullProfile[sbid] = createProfile(sbid, texture)
                    }

                    logger.info("PackDisabler finished loading ${idToLocation.size} items")
                }
                catch (e: Exception) {
                    logger.error("Failed to fetch Skyblock items", e)
                }
            }
        }
    }

    fun createProfile(sbid: String, texture: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))
        val profile = GameProfile(UUID.nameUUIDFromBytes("@MODID@:$sbid".toByteArray()), this::class.simpleName, properties)
        return ResolvableProfile.createResolved(profile)
    }
}