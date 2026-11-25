package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.Utils.uuid
import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.managers.WLM
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.tags.ItemTags

object DyeCommand {
    private val hexColor = Regex("#?[0-9a-fA-F]{6}")

    fun build() = ClientCommands.literal("dye").apply {
        then(setCommand())
        then(removeCommand())
        then(listCommand())
    }

    private fun setCommand() = ClientCommands.literal("set").then(
        ClientCommands.argument("color", StringArgumentType.word()).executes { context ->
            val stack = Minecraft.getInstance().player?.mainHandItem
            val target = stack?.uuid ?: run {
                chat("§cHeld item has no SkyBlock UUID!§r")
                return@executes Command.SINGLE_SUCCESS
            }

            val item = Config.replacements[WLM.id(stack)]?.let {
                BuiltInRegistries.ITEM.getValue(Identifier.parse(it)).defaultInstance
            } ?: stack


            if (! item.`is`(ItemTags.CAULDRON_CAN_REMOVE_DYE)) {
                chat("§cHeld item is not dyeable!§r")
                return@executes Command.SINGLE_SUCCESS
            }

            val color = StringArgumentType.getString(context, "color")
            if (! hexColor.matches(color)) {
                chat("§cInvalid color: §e$color§c. Use a hex color like §e#FF6600§c.§r")
                return@executes Command.SINGLE_SUCCESS
            }

            val rgb = color.removePrefix("#").uppercase()
            Config.armorDyes[target] = rgb
            chat("Armor dye for §e$target§r: §e#$rgb§r.")
            Command.SINGLE_SUCCESS
        }
    )

    private fun removeCommand() = ClientCommands.literal("remove").executes { _ ->
        val target = Minecraft.getInstance().player?.mainHandItem?.uuid ?: run {
            chat("§cHeld item has no SkyBlock UUID!§r")
            return@executes Command.SINGLE_SUCCESS
        }

        Config.armorDyes.remove(target)
        chat("Removed armor dye override for §e$target§r.")
        Command.SINGLE_SUCCESS
    }

    private fun listCommand(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("list").executes { _ ->
        if (Config.armorDyes.isEmpty()) chat("No armor dye overrides set.")
        else chat(buildString {
            appendLine("§eCurrent armor dye overrides:§r")
            Config.armorDyes.forEach { (target, color) -> appendLine("§b$target§r -> §a#$color§r") }
        })

        Command.SINGLE_SUCCESS
    }
}