package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.config.Config
import com.mojang.brigadier.Command
//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
//?}
import net.minecraft.client.Minecraft

object TextColorsCommand {
    fun build() = ClientCommands.literal("textcolors").executes {
        val enabled = ! Config.newTextColors
        Config.newTextColors = enabled
        Minecraft.getInstance().reloadResourcePacks()

        chat("New text colors: ${if (enabled) "§aON" else "§cOFF"}§r")
        Command.SINGLE_SUCCESS
    }
}