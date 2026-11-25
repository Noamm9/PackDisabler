package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.managers.WLM
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.equipment.Equippable
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(HumanoidArmorLayer::class)
abstract class MixinHumanoidArmorLayer {
    @WrapOperation(
        method = ["renderArmorPiece"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
        )]
    )
    private fun replaceArmorAsset(instance: ItemStack, dataComponentType: DataComponentType<*>, original: Operation<Equippable?>): Any? {
        val currentEquippable = original.call(instance, dataComponentType)
        val replacement = Config.replacements[WLM.id(instance)] ?: return currentEquippable
        val equippable = BuiltInRegistries.ITEM.getValue(Identifier.parse(replacement)).components()[DataComponents.EQUIPPABLE]
        return if (equippable?.assetId()?.isPresent != true) currentEquippable else equippable
    }
}
