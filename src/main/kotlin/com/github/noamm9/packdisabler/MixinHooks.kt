package com.github.noamm9.packdisabler

import com.github.noamm9.packdisabler.Utils.customData
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import net.minecraft.client.gui.screens.LoadingOverlay
import net.minecraft.client.gui.screens.Overlay
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object MixinHooks {
    @JvmField var isLoading = false

    @JvmStatic
    fun itemModelHook(stack: ItemStack, key: DataComponentType<*>, original: Operation<Identifier>): Identifier {
        val currentModel = original.call(stack, key)
        if (stack.isEmpty) return currentModel
        val skyblockID = stack.skyblockId

        if (currentModel.namespace != "hypixel_skyblock") return currentModel

        val customData = stack.customData

        val oldModel = when {
            skyblockID.isNotEmpty() -> PackDisabler.idToLocation[stack.skyblockId]
            customData.contains("quiver_arrow") -> Items.ARROW.components()[DataComponents.ITEM_MODEL]
            else -> null
        }

        return oldModel ?: currentModel
    }

    @JvmStatic
    fun skullProfileHook(stack: ItemStack, key: DataComponentType<*>, original: Operation<ResolvableProfile>): ResolvableProfile {
        val currentProfile = original.call(stack, key)
        if (stack.isEmpty) return currentProfile
        if (key != DataComponents.PROFILE) return currentProfile

        val skyblockID = stack.skyblockId
        if (skyblockID.isEmpty()) return currentProfile

        val profile = PackDisabler.idToSkullProfile[skyblockID]
        return profile ?: currentProfile
    }

    @JvmStatic
    fun setOverlayHook(overlay: Overlay?, ci: CallbackInfo) {
        if (overlay is LoadingOverlay && isLoading) ci.cancel()
        else if (overlay == null && isLoading) isLoading = false
    }

    @JvmStatic fun resourcePackPushHook() {
        isLoading = true
    }
}