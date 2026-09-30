package com.github.noamm9.packdisabler.commands


import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.commands.impl.*
import com.github.noamm9.packdisabler.config.managers.WLM
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ModCommands {
    private val usage = mapOf(
        "/@MODID@ help" to "Show this message.",
        "/@MODID@ reload" to "Download + reload pack.",
        "/@MODID@ textcolors" to "Toggle new text colors.",
        "/@MODID@ whitelist [SkyBlock ID]" to "Toggle Hypixel texture.",
        "/@MODID@ replace set <vanilla item>" to "Set vanilla look.",
        "/@MODID@ replace remove" to "Remove replacement + glint.",
        "/@MODID@ replace list" to "List replacements.",
        "/@MODID@ replace export" to "Copy all to clipboard.",
        "/@MODID@ replace import" to "Replace all from clipboard.",
        "/@MODID@ glint set <on|off>" to "Set replacement glint.",
        "/@MODID@ glint remove" to "Reset glint override.",
        "/@MODID@ glint list" to "List glint overrides.",
        "/@MODID@ debug" to "Toggle data copy on whitelist key.",
    )

    fun printFirstUseMessage() {
        chat(buildString {
            appendLine("§fSkyBlock textures disabled automatically.")
            appendLine("Your own resource packs still work.")
            appendLine("Hover an inventory item and press §e${WLM.keybind.translatedKeyMessage.string}§f to toggle its texture.")
            appendLine("§e/@MODID@ whitelist [ID] §f- Toggle held item/ID texture.")
            appendLine("§e/@MODID@ replace set <item> §f- Change held item's look.")
            appendLine("§e/@MODID@ replace remove §f- Reset held item's look.")
            appendLine("§e/@MODID@ textcolors §f- Toggle new text colors.")
            appendLine("§e/@MODID@ help §f- All commands + usage.")
            appendLine("§7<item> = vanilla ID, e.g. diamond_sword.§f")
        }.trimEnd())
    }

    fun printHelp(): Int {
        chat(buildString {
            appendLine("§fCommands + usage")
            appendLine("Item changes affect the held SkyBlock item.")
            appendLine("§7<...> required; [...] optional.")
            appendLine("Vanilla item example: diamond_sword.§f")
            usage.forEach { (usage, desc) -> appendLine("§e$usage §f- $desc") }
        }.trimEnd())
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
