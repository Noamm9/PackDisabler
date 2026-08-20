package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.PackDisabler.Companion.debug
import com.github.noamm9.packdisabler.Utils.chat
import com.mojang.brigadier.Command
import net.fabricmc.fabric.api.client.command.v2.ClientCommands

object DebugCommand {
    fun build() = ClientCommands.literal("debug").executes {
        debug = debug.not()
        chat("Debug mode: $debug")
        Command.SINGLE_SUCCESS
    }
}