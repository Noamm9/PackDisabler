package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.MixinHooks.isLoading
import com.github.noamm9.packdisabler.ResourceOverrides
import com.github.noamm9.packdisabler.config.Config
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl
import net.minecraft.network.DisconnectionDetails
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientCommonPacketListenerImpl::class)
class MixinClientCommonPacketListenerImpl {
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
        if (Config.hidePackDownloadScreen) isLoading = true
        ResourceOverrides.addPack(packet.id)

        if (! Config.blockPackDownload) return
        val connection = Minecraft.getInstance().connection ?: return
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.ACCEPTED))
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED))
        ci.cancel()
    }

    @Inject(
        method = ["handleResourcePackPop"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER
        )]
    )
    private fun onResourcePackPop(packet: ClientboundResourcePackPopPacket, ci: CallbackInfo?) {
        if (ResourceOverrides.removePack(packet.id.orElse(null))) isLoading = false
    }

    @Inject(method = ["onDisconnect"], at = [At("HEAD")])
    private fun onDisconnect(details: DisconnectionDetails?, ci: CallbackInfo?) {
        ResourceOverrides.clear()
        isLoading = false
    }
}