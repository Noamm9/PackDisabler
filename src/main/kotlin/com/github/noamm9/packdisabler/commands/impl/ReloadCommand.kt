package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.HypixelPackLoader
import com.github.noamm9.packdisabler.Utils.chat
import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ReloadCommand {
    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("reload").executes {
        chat("§7Reloading the Hypixel texture pack...§r")
        HypixelPackLoader.reload { success ->
            chat(if (success) "§aHypixel texture pack reloaded.§r" else "§cFailed to reload the Hypixel texture pack. Check the log for details.§r")
        }
        Command.SINGLE_SUCCESS
    }
}