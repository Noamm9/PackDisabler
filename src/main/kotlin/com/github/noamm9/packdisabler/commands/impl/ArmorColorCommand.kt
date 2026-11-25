package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.Utils.uuid
import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.managers.WLM
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.minecraft.client.Minecraft
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.tags.ItemTags

object ArmorColorCommand {
    private val hexColor = Regex("#?[0-9a-fA-F]{6}")

    fun build() = ClientCommands.literal("armorcolor").apply {
        then(setCommand())
        then(removeCommand())
    }

    private fun setCommand() = ClientCommands.literal("set").then(
        ClientCommands.argument("Hex (RGB)", StringArgumentType.word()).executes { context ->
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

            val color = StringArgumentType.getString(context, "Hex (RGB)")
                .removePrefix("#").take(6).uppercase()

            if (! hexColor.matches(color)) {
                chat("§cInvalid color: §e$color§c. Use a hex color like §e#FF6600§c.§r")
                return@executes Command.SINGLE_SUCCESS
            }

            Config.armorColors[target] = color
            chat("Armor color set to: §e#$color§r.")
            Command.SINGLE_SUCCESS
        }
    )

    private fun removeCommand() = ClientCommands.literal("remove").executes { _ ->
        val target = Minecraft.getInstance().player?.mainHandItem?.uuid ?: run {
            chat("§cHeld item has no SkyBlock UUID!§r")
            return@executes Command.SINGLE_SUCCESS
        }

        Config.armorColors.remove(target)
        chat("Removed armor color override.")
        Command.SINGLE_SUCCESS
    }
}