package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.ResourceOverrides
import com.github.noamm9.packdisabler.config.Config
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl
import net.minecraft.network.Connection
import net.minecraft.network.DisconnectionDetails
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientCommonPacketListenerImpl::class)
abstract class MixinClientCommonPacketListenerImpl {
    @Shadow @Final private lateinit var connection: Connection

    @Inject(
        method = ["handleResourcePackPush"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER
        )],
        cancellable = true
    )
    private fun onResourcePack(packet: ClientboundResourcePackPushPacket, ci: CallbackInfo) {
        if (! packet.url.contains("hypixel.net") || ! packet.url.contains("SkyBlock")) return
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.ACCEPTED))
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED))
        ci.cancel()
    }
}