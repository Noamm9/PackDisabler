package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.config.WhitelistManager
import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper as KeyMappingHelper
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
//?}
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*
import javax.net.ssl.HttpsURLConnection

@Entrypoint(Entrypoint.CLIENT)
class PackDisabler: ClientModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger(PackDisabler::class.java)
        var idToLocation = HashMap<String, Identifier>()
        val idToSkullProfile = HashMap<String, ResolvableProfile>()
    }

    override fun onInitializeClient() {
        HypixelPackLoader.init()
        //? if =1.21.11 {
        /*KeyMappingHelper.registerKeyBinding(WhitelistManager.keybind)
        *///?} else {
        KeyMappingHelper.registerKeyMapping(WhitelistManager.keybind)
        //?}

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommands.literal("@MODID@").then(ClientCommands.literal("whitelist").apply {
                executes {
                    val player = Minecraft.getInstance().player ?: return@executes 0
                    val sbid = player.mainHandItem.skyblockId
                    if (sbid == null) {
                        chat("§cHeld item has no Skyblock ID!§r")
                        return@executes 0
                    }
                    WhitelistManager.toggle(sbid)
                    Command.SINGLE_SUCCESS
                }

                then(ClientCommands.argument("sbid", StringArgumentType.string()).apply {
                    suggests { _, builder ->
                        idToLocation.keys.filter { it.startsWith(builder.remaining, ignoreCase = true) }.forEach(builder::suggest)
                        builder.buildFuture()
                    }

                    executes { context ->
                        val sbid = StringArgumentType.getString(context, "sbid")
                        WhitelistManager.toggle(sbid)
                        Command.SINGLE_SUCCESS
                    }
                })
            }))
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                logger.info("Fetching Skyblock items from API")
                val url = URI.create("https://api.noamm.org/resources/skyblock-items").toURL()
                val connection = url.openConnection() as HttpsURLConnection
                connection.setRequestProperty("User-Agent", this::class.simpleName)
                connection.requestMethod = "GET"
                val raw = connection.inputStream.bufferedReader().readText()

                for ((sbid, element) in Json.parseToJsonElement(raw).jsonObject) {
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

    private fun createProfile(sbid: String, texture: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))
        val profile = GameProfile(UUID.nameUUIDFromBytes("@MODID@:$sbid".toByteArray()), this::class.simpleName, properties)
        return ResolvableProfile.createResolved(profile)
    }
}