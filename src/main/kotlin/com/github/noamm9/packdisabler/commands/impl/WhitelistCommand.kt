package com.github.noamm9.packdisabler.commands.impl

import com.github.noamm9.packdisabler.PackDisabler
import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.config.managers.WLM
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
//? if =1.21.11 {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager as ClientCommands
*///?} else {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
//?}
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft

object WhitelistCommand {
    fun build(): LiteralArgumentBuilder<FabricClientCommandSource> = ClientCommands.literal("whitelist").apply {
        executes {
            val sbid = WLM.id(Minecraft.getInstance().player?.mainHandItem) ?: run {
                chat("§cHeld item has no Skyblock ID!§r")
                return@executes Command.SINGLE_SUCCESS
            }
            WLM.toggle(sbid)
            Command.SINGLE_SUCCESS
        }

        then(ClientCommands.argument("SkyBlock ID", StringArgumentType.string()).apply {
            suggests { _, builder ->
                PackDisabler.idToLocation.keys.filter { it.startsWith(builder.remaining, ignoreCase = true) }.forEach(builder::suggest)
                builder.buildFuture()
            }

            executes { context ->
                val sbid = StringArgumentType.getString(context, "SkyBlock ID")
                WLM.toggle(sbid)
                Command.SINGLE_SUCCESS
            }
        })
    }
}