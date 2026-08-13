package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.commands.ModCommands
import com.github.noamm9.packdisabler.config.managers.WLM
import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper as KeyMappingHelper
*///?} else {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
//?}
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
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
        val vanillaItemModels by lazy(LazyThreadSafetyMode.NONE) {
            buildMap {
                BuiltInRegistries.ITEM.forEach { item ->
                    val id = BuiltInRegistries.ITEM.getKey(item).toString()
                    if (! id.startsWith("minecraft:")) return@forEach
                    item.components()[DataComponents.ITEM_MODEL]?.let { put(id, it) }
                }
            }
        }
        var debug = false
    }

    override fun onInitializeClient() {
        //? if =1.21.11 {
        /*KeyMappingHelper.registerKeyBinding(WLM.keybind)
        *///?} else {
        KeyMappingHelper.registerKeyMapping(WLM.keybind)
        //?}

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            ModCommands.register(dispatcher)
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