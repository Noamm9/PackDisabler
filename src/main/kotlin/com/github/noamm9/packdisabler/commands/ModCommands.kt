package com.github.noamm9.packdisabler.commands


import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.commands.impl.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ModCommands {
    private val usage = mapOf(
        "/@MODID@ reload" to "Download and reload the Hypixel texture pack.",
        "/@MODID@ textcolors" to "Toggle the new Hypixel text colors.",
        "/@MODID@ whitelist" to "Toggle the pack override for an item.",
        "/@MODID@ replace set <vanilla item>" to "Visually replace the held SkyBlock item with a vanilla item.",
        "/@MODID@ replace remove" to "Remove the visual replacement from the held item.",
        "/@MODID@ replace list" to "List all visual replacements.",
        "/@MODID@ glint set <on|off>" to "Override the enchantment glint for the held item's replacement.",
        "/@MODID@ replace import|export" to "Share visual replacements through the clipboard.",
        "/@MODID@ debug" to "prints item data to chat when adding an item to the whitelist.",
    )

    fun printHelp(): Int {
        usage.forEach { (usage, desc) -> chat("§7$usage §f- $desc") }
        return Command.SINGLE_SUCCESS
    }

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(ClientCommands.literal("@MODID@").apply {
            executes { printHelp() }
            then(ClientCommands.literal("help").executes { printHelp() })
            then(DebugCommand.build())
            then(ReloadCommand.build())
            then(TextColorsCommand.build())
            then(WhitelistCommand.build())
            then(GlintCommand.build())
            then(ReplaceCommand.build())
        })
    }
}
