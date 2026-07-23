package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.DynamicItemModels
import com.github.noamm9.packdisabler.PackDisabler
import com.github.noamm9.packdisabler.Utils.customData
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.WLM
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

@Mixin(ItemModelResolver::class)
abstract class MixinItemModelResolver {
    private val `packdisabler$arrow` by lazy(LazyThreadSafetyMode.NONE) { Items.ARROW.components()[DataComponents.ITEM_MODEL] }

    @WrapOperation(
        method = ["appendItemLayers"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"
        )]
    )
    private fun appendItemLayerHook(instance: ItemStack, dataComponentType: DataComponentType<*>, original: Operation<Identifier>): Any {
        val currentModel = original.call(instance, dataComponentType)
        if (instance.isEmpty) return currentModel
        if (currentModel.namespace != "hypixel_skyblock") return currentModel
        if (WLM.id(instance) in Config.whitelist) return currentModel

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