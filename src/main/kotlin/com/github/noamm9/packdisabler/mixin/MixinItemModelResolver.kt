package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.DynamicItemModels
import com.github.noamm9.packdisabler.PackDisabler
import com.github.noamm9.packdisabler.Utils.customData
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.managers.WLM
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import net.minecraft.client.renderer.item.ItemModelResolver
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(ItemModelResolver::class)
abstract class MixinItemModelResolver {
    private val `packdisabler$arrow` by lazy(LazyThreadSafetyMode.NONE) { Items.ARROW.components()[DataComponents.ITEM_MODEL] }

    @ModifyVariable(method = ["appendItemLayers"], at = At("HEAD"), argsOnly = true)
    private fun applyReplacementGlint(stack: ItemStack): ItemStack {
        if (WLM.id(stack) !in Config.replacementGlints) return stack

        return stack.copy().apply { set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true) }
    }

    @WrapOperation(
        method = ["appendItemLayers"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
        )]
    )
    private fun appendItemLayerHook(instance: ItemStack, dataComponentType: DataComponentType<*>, original: Operation<Identifier?>): Any? {
        val currentModel = original.call(instance, dataComponentType) ?: return null
        val id = WLM.id(instance)

        id?.let(Config.replacements::get)?.let(PackDisabler.vanillaItemModels::get)?.let { return it }
        if (currentModel.namespace != "hypixel_skyblock") return currentModel
        if (id in Config.whitelist) return currentModel

        val customData = instance.customData
        val skyblockID = skyblockId(customData)

        val oldModel = when {
            skyblockID != null -> PackDisabler.idToLocation[skyblockID]
            customData.contains("quiver_arrow") -> `packdisabler$arrow`
            else -> null
        } ?: return currentModel

        return skyblockID?.let { DynamicItemModels.resolve(it, instance, customData, oldModel) } ?: oldModel
    }
}