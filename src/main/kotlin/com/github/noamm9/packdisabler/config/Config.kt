package com.github.noamm9.packdisabler.config

import com.github.noamm9.packdisabler.PackDisabler
import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.platform.YACLPlatform
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class Config {
    @SerialEntry var blockPackDownload = true

    companion object {
        val handler = ConfigClassHandler.createBuilder(Config::class.java).serializer {
            GsonConfigSerializerBuilder.create(it).setPath(YACLPlatform.getConfigDir().resolve("@MODID@").resolve("config.json")).build()
        }.build()

        fun createScreen(parent: Screen?) = YetAnotherConfigLib.create(handler) { defaults, config, builder ->
            builder.title(Component.literal("Pack Disabler")).category(ConfigCategory.createBuilder().apply {
                name(Component.literal("General"))

                option(Option.createBuilder<Boolean>().apply {
                    name(Component.literal("Block Pack Download"))
                    description(OptionDescription.of(Component.literal("Blocks the resource pack download packet sent by Hypixel when joining Skyblock.")))
                    binding(defaults.blockPackDownload, { config.blockPackDownload }, { config.blockPackDownload = it })
                    controller(BooleanControllerBuilder::create)
                }.build())

                option(ButtonOption.createBuilder().apply {
                    name(Component.literal("Clear Cache"))
                    description(OptionDescription.of(Component.literal("Clears the cache file of the item api.")))
                    action { _, _ ->
                        PackDisabler.cacheDir.toFile().deleteRecursively()
                        PackDisabler.logger.info("Cleared cache file")
                    }
                }.build())
            }.build())
        }.generateScreen(parent)
    }
}