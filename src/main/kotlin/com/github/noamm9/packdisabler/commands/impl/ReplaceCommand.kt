package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.PackDisabler
import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.managers.RM
import com.github.noamm9.packdisabler.config.managers.WLM
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft

object ReplaceCommand {
    fun build() = ClientCommands.literal("replace").apply {
        then(setCommand())
        then(removeCommand())
        then(listCommand())
        then(exportCommand())
        then(importCommand())
    }

    private fun setCommand() = ClientCommands.literal("set").then(ClientCommands.argument("replacement", StringArgumentType.greedyString()).apply {
        suggests { _, builder ->
            val remaining = builder.remaining.substringAfter(':')
            PackDisabler.vanillaItemModels.keys.asSequence()
                .map { it.removePrefix("minecraft:") }
                .filter { it.startsWith(remaining, ignoreCase = true) }
                .forEach(builder::suggest)
            builder.buildFuture()
        }

        executes { context ->
            val target = WLM.id(Minecraft.getInstance().player?.mainHandItem) ?: run {
                chat("§cHeld item has no SkyBlock ID!§r")
                return@executes Command.SINGLE_SUCCESS
            }

            val replacementInput = StringArgumentType.getString(context, "replacement")
            val replacement = "minecraft:${replacementInput.lowercase()}"

            if (replacement !in PackDisabler.vanillaItemModels) {
                chat("§cUnknown vanilla item ID: §e$replacementInput§c.§r")
                return@executes Command.SINGLE_SUCCESS
            }

            Config.replacements[target] = replacement
            chat("§e$target§r now looks like §e$replacement§r.")

            Command.SINGLE_SUCCESS
        }
    })

    private fun removeCommand() = ClientCommands.literal("remove").executes { _ ->
        val target = WLM.id(Minecraft.getInstance().player?.mainHandItem) ?: run {
            chat("§cHeld item has no SkyBlock ID!§r")
            return@executes Command.SINGLE_SUCCESS
        }

        Config.replacements.remove(target)
        Config.replacementGlints.remove(target)
        chat("Removed visual replacement for §e$target§r.")
        Command.SINGLE_SUCCESS
    }


    private fun listCommand(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("list").executes { _ ->
        if (Config.replacements.isEmpty()) chat("No visual replacements set.")
        else chat(buildString {
            appendLine("§eCurrent visual replacements:§r")
            Config.replacements.forEach { (target, replacement) -> appendLine("§b$target§r -> §a$replacement§r") }
        })

        Command.SINGLE_SUCCESS
    }


    private fun exportCommand(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("export").executes {
        Minecraft.getInstance().keyboardHandler.clipboard = RM.encode(Config.replacements)
        chat("§aCopied ${Config.replacements.size} visual replacement(s) to the clipboard.§r")
        Command.SINGLE_SUCCESS
    }

    private fun importCommand(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("import").executes {
        val clipboard = Minecraft.getInstance().keyboardHandler.clipboard.trim()
        val replacements = RM.decode(clipboard)
        if (replacements == null || replacements.any { (sbid, model) -> sbid !in PackDisabler.idToLocation || model !in PackDisabler.vanillaItemModels }) {
            chat("§cFailed to import visual replacements. Is the clipboard data valid?§r")
            return@executes Command.SINGLE_SUCCESS
        }

        Config.replacements.clear()
        Config.replacements.putAll(replacements)
        Config.replacementGlints.keys.filter { it !in replacements.keys }.forEach { Config.replacementGlints.remove(it) }
        chat("§aImported ${replacements.size} visual replacement(s) from the clipboard.§r")
        Command.SINGLE_SUCCESS
    }
}