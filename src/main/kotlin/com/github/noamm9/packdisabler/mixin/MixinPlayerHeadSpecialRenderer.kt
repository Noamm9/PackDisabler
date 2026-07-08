package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.PackDisabler
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.Utils.skyblockSkinId
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(PlayerHeadSpecialRenderer::class)
class MixinPlayerHeadSpecialRenderer {
    @WrapOperation(
        method = [$$"extractArgument(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/renderer/PlayerSkinRenderCache$RenderInfo;"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
        )]
    )
    private fun replaceSkyblockHeadProfile(instance: ItemStack, dataComponentType: DataComponentType<*>, original: Operation<ResolvableProfile?>): Any? {
        val currentProfile = original.call(instance, dataComponentType)
        if (instance.isEmpty) return currentProfile
        return instance.skyblockSkinId?.let(PackDisabler.idToSkullProfile::get)
            ?: instance.skyblockId?.let(PackDisabler.idToSkullProfile::get)
            ?: currentProfile
    }
}