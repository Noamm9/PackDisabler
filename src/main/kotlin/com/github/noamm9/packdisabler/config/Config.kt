package com.github.noamm9.packdisabler.config

import dev.isxander.yacl3.api.*
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder
import dev.isxander.yacl3.platform.YACLPlatform
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.jvm.java

class Config {
    @SerialEntry var myCoolBoolean = true
    @SerialEntry var myCoolInteger = 5
    @SerialEntry(comment = "This string is amazing") var myCoolString = "How amazing!"

    companion object {
        val handler = ConfigClassHandler.createBuilder(Config::class.java).serializer {
            GsonConfigSerializerBuilder.create(it).setPath(
                YACLPlatform.getConfigDir().resolve("@MODID@.json5")
            ).build()
        }.build()

        fun createScreen(parent: Screen?) = YetAnotherConfigLib.create(handler) { defaults, config, builder ->
            builder.apply {
                title(Component.literal("Used for narration. Could be used to render a title in the future."))
                category(ConfigCategory.createBuilder().apply {
                    name(Component.literal("Name of the category"))
                    tooltip(Component.literal("This text will appear as a tooltip when you hover or focus the button with Tab. There is no need to add \n to wrap as YACL will do it for you."))
                    group(OptionGroup.createBuilder().apply {
                        name(Component.literal("Name of the group"))
                        description(OptionDescription.of(Component.literal("This text will appear when you hover over the name or focus on the collapse button with Tab.")))
                        option(Option.createBuilder<Boolean>().apply {
                            name(Component.literal("Boolean Option"))
                            description(OptionDescription.of(Component.literal("This text will appear as a tooltip when you hover over the option.")))
                            binding(defaults.myCoolBoolean, { config.myCoolBoolean }, { config.myCoolBoolean = it })
                            controller(TickBoxControllerBuilder::create)
                        }.build()).build()
                    }.build()).build()
                }.build()).build()
            }
        }.generateScreen(parent)
    }
}