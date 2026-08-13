package com.github.noamm9.packdisabler.commands.impl

//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
//?}
import com.github.noamm9.packdisabler.PackDisabler.Companion.debug
import com.github.noamm9.packdisabler.Utils.chat
import com.mojang.brigadier.Command

object DebugCommand {
    fun build() = ClientCommands.literal("debug").executes {
        debug = debug.not()
        chat("Debug mode: $debug")
        Command.SINGLE_SUCCESS
    }
}