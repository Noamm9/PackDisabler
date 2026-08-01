package com.github.noamm9.packdisabler

//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper as KeyMappingHelper
*///?} else {
//?}
import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.config.WLM
import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.world.item.component.ResolvableProfile
import org.slf4j.LoggerFactory
import java.util.*

@Entrypoint(Entrypoint.CLIENT)
class PackDisabler: ClientModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger(PackDisabler::class.java)
        var idToLocation = HashMap<String, Identifier>()
        val idToSkullProfile = HashMap<String, ResolvableProfile>()
        var debug = false
    }

    override fun onInitializeClient() {
        //? if =1.21.11 {
        /*KeyMappingHelper.registerKeyBinding(WLM.keybind)
        *///?} else {
        KeyMappingHelper.registerKeyMapping(WLM.keybind)
        //?}

        val commandUsage = mapOf(
            "/@MODID@ reload" to "Download and reload the Hypixel texture pack.",
            "/@MODID@ whitelist" to "Toggle the pack override for an item.",
            "/@MODID@ debug" to "prints item data to chat when adding an item to the whitelist.",
        )

        fun printHelp(): Int {
            commandUsage.forEach { (usage, desc) -> chat("§7$usage §f- $desc") }
            return Command.SINGLE_SUCCESS
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(ClientCommands.literal("@MODID@").apply {
                executes { printHelp() }

                then(ClientCommands.literal("help").executes { printHelp() })
                then(ClientCommands.literal("debug").executes {
                    debug = debug.not()
                    chat("Debug mode: $debug")
                    Command.SINGLE_SUCCESS
                })

                then(ClientCommands.literal("reload").executes {
                    chat("§7Reloading the Hypixel texture pack...§r")
                    HypixelPackLoader.reload { success ->
                        chat(if (success) "§aHypixel texture pack reloaded.§r" else "§cFailed to reload the Hypixel texture pack. Check the log for details.§r")
                    }
                    Command.SINGLE_SUCCESS
                })

                then(ClientCommands.literal("whitelist").apply {
                    executes {
                        val sbid = WLM.id(Minecraft.getInstance().player?.mainHandItem) ?: run {
                            chat("§cHeld item has no Skyblock ID!§r")
                            return@executes Command.SINGLE_SUCCESS
                        }
                        WLM.toggle(sbid)
                        Command.SINGLE_SUCCESS
                    }

                    then(ClientCommands.argument("SkyBlock ID", StringArgumentType.string()).apply {
                        suggests { _, builder ->
                            idToLocation.keys.filter { it.startsWith(builder.remaining, ignoreCase = true) }.forEach(builder::suggest)
                            builder.buildFuture()
                        }

                        executes { context ->
                            val sbid = StringArgumentType.getString(context, "SkyBlock ID")
                            WLM.toggle(sbid)
                            Command.SINGLE_SUCCESS
                        }
                    })
                })
            })
        }

        val raw = this::class.java.getResourceAsStream("/skyblock-items.json")?.reader()?.readText() ?: error("missing skyblock-items.json")

        for ((sbid, element) in Json.parseToJsonElement(raw).jsonObject) {
            val item = element.jsonObject
            val model = item["model"]?.jsonPrimitive?.content ?: continue
            val texture = item["texture"]?.jsonPrimitive?.content

            idToLocation[sbid] = Identifier.parse(model)
            if (! texture.isNullOrEmpty()) idToSkullProfile[sbid] = createProfile(sbid, texture)
        }

        logger.info("Finished loading ${idToLocation.size} items")
    }

    private fun createProfile(sbid: String, texture: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", texture)))
        val profile = GameProfile(UUID.nameUUIDFromBytes("@MODID@:$sbid".toByteArray()), this::class.simpleName, properties)
        return ResolvableProfile.createResolved(profile)
    }
}