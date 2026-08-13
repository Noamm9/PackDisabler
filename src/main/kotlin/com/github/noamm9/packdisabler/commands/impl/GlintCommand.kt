package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.managers.WLM
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
//?}
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft

object GlintCommand {
    fun build() = ClientCommands.literal("glint").apply {
        then(setCommand())
        then(removeCommand())
        then(listCommand())
    }

    private fun setCommand() = ClientCommands.literal("set").then(
        ClientCommands.argument("state", StringArgumentType.word()).suggests { _, builder ->
            listOf("on", "off").forEach(builder::suggest)
            builder.buildFuture()
        }.executes { context ->
            val target = WLM.id(Minecraft.getInstance().player?.mainHandItem) ?: run {
                chat("§cHeld item has no Skyblock ID!§r")
                return@executes Command.SINGLE_SUCCESS
            }

            val state = StringArgumentType.getString(context, "state").lowercase()
            if (state != "on" && state != "off") {
                chat("§cInvalid glint state: §e$state§c. Use §eon§c or §eoff§c.§r")
                return@executes Command.SINGLE_SUCCESS
            }

            Config.replacementGlints[target] = state
            chat("Enchantment glint for §e$target§r: §e$state§r.")
            Command.SINGLE_SUCCESS
        }
    )

    private fun removeCommand() = ClientCommands.literal("remove").executes { _ ->
        val target = WLM.id(Minecraft.getInstance().player?.mainHandItem) ?: run {
            chat("§cHeld item has no Skyblock ID!§r")
            return@executes Command.SINGLE_SUCCESS
        }

        Config.replacementGlints.remove(target)
        chat("Removed enchantment glint override for §e$target§r.")
        Command.SINGLE_SUCCESS
    }

    private fun listCommand(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("list").executes { _ ->
        if (Config.replacementGlints.isEmpty()) chat("No enchantment glint overrides set.")
        else chat(buildString {
            appendLine("§eCurrent enchantment glint overrides:§r")
            Config.replacementGlints.forEach { (target, state) -> appendLine("§b$target§r -> §a$state§r") }
        })

        Command.SINGLE_SUCCESS
    }
}