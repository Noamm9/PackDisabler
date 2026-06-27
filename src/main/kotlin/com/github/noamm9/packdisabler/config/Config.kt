package com.github.noamm9.packdisabler.config

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.platform.YACLPlatform
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class Config {
    @SerialEntry var blockPackDownload = false
    @SerialEntry var revertItems = false
    @SerialEntry var revertTooltip = false

    companion object {
        val handler = ConfigClassHandler.createBuilder(Config::class.java).serializer {
            GsonConfigSerializerBuilder.create(it).setPath(YACLPlatform.getConfigDir().resolve("@MODID@").resolve("config.json")).build()
        }.build()

        val INSTANCE get() = handler.instance()

        fun createScreen(parent: Screen?) = YetAnotherConfigLib.create(handler) { defaults, config, builder ->
            builder.title(Component.literal("Pack Disabler")).category(ConfigCategory.createBuilder().apply {
                name(Component.literal("General"))

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.literal("Block Pack Download"))
                    description(OptionDescription.of(Component.literal("Blocks the resource pack download packet sent by Hypixel when joining Skyblock.")))
                    binding(defaults.blockPackDownload, { config.blockPackDownload }, { config.blockPackDownload = it })
                    controller(BooleanControllerBuilder::create)
                }.build())

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.literal("Revert Item Textures"))
                    description(OptionDescription.of(Component.literal("Converts Skyblock item textures back to their vanilla equivalents.")))
                    binding(defaults.revertItems, { config.revertItems }, { config.revertItems = it })
                    controller(BooleanControllerBuilder::create)
                }.build())

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.literal("Revert Tooltip Textures"))
                    description(OptionDescription.of(Component.literal("Converts Skyblock tooltip textures back to their vanilla equivalents. Requires Block Pack Download to be enabled")))
                    binding(defaults.revertTooltip, { config.revertTooltip }, { config.revertTooltip = it })
                    controller(BooleanControllerBuilder::create)
                }.build())

            }.build())
        }.generateScreen(parent)
    }
}