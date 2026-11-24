package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.Utils.uuid
import com.github.noamm9.packdisabler.config.Config
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.core.component.DataComponents

object DyeCommand {
    private val hexColor = Regex("[0-9a-fA-F]{6}")

    fun build() = ClientCommands.literal("dye").apply {
        then(setCommand())
        then(removeCommand())
        then(listCommand())
    }

    private fun setCommand() = ClientCommands.literal("set").then(
        ClientCommands.argument("color", StringArgumentType.greedyString()).executes { context ->
            val color = StringArgumentType.getString(context, "color").trim().removePrefix("#")
            if (!hexColor.matches(color)) return@executes fail("Enter a six-digit hex color, e.g. #FF6600.")

            val stack = context.source.player.mainHandItem
            val target = stack.uuid ?: return@executes fail("Held item has no SkyBlock UUID.")
            val equipment = stack.get(DataComponents.EQUIPPABLE)
            if (equipment == null || !equipment.slot().isArmor || equipment.assetId().isEmpty) {
                return@executes fail("Hold a SkyBlock armor piece with an armor model.")
            }

            val rgb = color.uppercase()
            Config.armorDyes[target] = rgb
            chat("Saved armor dye §e#$rgb§r for §e$target§r.")
            Command.SINGLE_SUCCESS
        }
    )

    private fun removeCommand() = ClientCommands.literal("remove").executes { context ->
        val target = context.source.player.mainHandItem.uuid ?: return@executes fail("Held item has no SkyBlock UUID.")

        if (Config.armorDyes.remove(target) != null) chat("Restored the original armor color for §e$target§r.")
        else chat("No armor dye override set for §e$target§r.")
        Command.SINGLE_SUCCESS
    }

    private fun listCommand() = ClientCommands.literal("list").executes {
        if (Config.armorDyes.isEmpty()) chat("No armor dye overrides set.")
        else chat(buildString {
            appendLine("§eCurrent armor dye overrides:§r")
            Config.armorDyes.forEach { (target, color) -> appendLine("§b$target§r -> §a#$color§r") }
        }.trimEnd())
        Command.SINGLE_SUCCESS
    }

    private fun fail(message: String): Int {
        chat("§c$message§r")
        return 0
    }
}
