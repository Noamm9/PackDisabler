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
class MixinClientCommonPacketListenerImpl {
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
        if (! packet.url.startsWith("https://resourcepacks2.hypixel.net/SkyBlockResourcePack/")) return
        ResourceOverrides.addPack(packet.id)
        if (! Config.blockPackDownload) return
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.ACCEPTED))
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED))
        ci.cancel()
    }

    @Inject(method = ["handleResourcePackPop"], at = [At(value = "TAIL")])
    private fun onResourcePackPop(packet: ClientboundResourcePackPopPacket, ci: CallbackInfo?) {
        ResourceOverrides.removePack(packet.id.orElse(null))
    }

    @Inject(method = ["onDisconnect"], at = [At("HEAD")])
    private fun onDisconnect(details: DisconnectionDetails?, ci: CallbackInfo?) {
        ResourceOverrides.clear()
    }
}