package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.Utils.customData
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.config.Config
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.LoadingOverlay
import net.minecraft.client.gui.screens.Overlay
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

/**
 * ideally it would be best to set the stack componnents to the old ones permentnty
 * however doing that could break future mods/texturepacks
 */
object MixinHooks {
    private var isLoading = false

    @JvmStatic
    fun itemModelHook(stack: ItemStack, key: DataComponentType<*>, original: Operation<Identifier>): Identifier {
        val currentModel = original.call(stack, key)
        if (! Config.INSTANCE.revertItems) return currentModel
        if (stack.isEmpty) return currentModel
        if (currentModel.namespace != "hypixel_skyblock") return currentModel

        val customData = stack.customData
        val skyblockID = skyblockId(customData)

        // quiver arrows have no skyblock id
        val oldModel = when {
            skyblockID != null -> PackDisabler.idToLocation[skyblockID]
            customData.contains("quiver_arrow") -> Items.ARROW.components()[DataComponents.ITEM_MODEL]
            else -> null
        }

        return oldModel ?: currentModel
    }

    @JvmStatic
    fun skullProfileHook(stack: ItemStack, key: DataComponentType<*>, original: Operation<ResolvableProfile>): ResolvableProfile {
        val currentProfile = original.call(stack, key)
        if (! Config.INSTANCE.revertItems) return currentProfile
        if (stack.isEmpty) return currentProfile
        val skyblockID = stack.skyblockId ?: return currentProfile

        val profile = PackDisabler.idToSkullProfile[skyblockID]
        return profile ?: currentProfile
    }

    @JvmStatic
    fun renderToolTipHook(font: Font, lines: MutableList<ClientTooltipComponent>, xo: Int, yo: Int, positioner: ClientTooltipPositioner, style: Identifier?, original: Operation<Void>) {
        val oldStyle = if (Config.INSTANCE.revertTooltip && style?.namespace == "hypixel_skyblock") null else style
        original.call(font, lines, xo, yo, positioner, oldStyle)
    }

    @JvmStatic
    fun setOverlayHook(overlay: Overlay?, ci: CallbackInfo) {
        if (overlay is LoadingOverlay && isLoading) ci.cancel()
        else if (overlay == null && isLoading) isLoading = false
    }

    @JvmStatic
    fun resourcePackPushHook(packet: ClientboundResourcePackPushPacket, ci: CallbackInfo) {
        if (! packet.url.startsWith("https://resourcepacks2.hypixel.net/SkyBlockResourcePack/")) return
        isLoading = true

        if (! Config.INSTANCE.blockPackDownload) return
        val connection = Minecraft.getInstance().connection ?: return
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.ACCEPTED))
        connection.send(ServerboundResourcePackPacket(packet.id, ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED))
        ci.cancel()
    }
}