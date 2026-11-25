package com.github.noamm9.packdisabler.mixin

import net.minecraft.client.renderer.entity.EntityRenderDispatcher
import net.minecraft.client.resources.model.EquipmentAssetManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(EntityRenderDispatcher::class)
interface IEntityRenderDispatcher {
    @get:Accessor("equipmentAssets")
    val equipmentAssets: EquipmentAssetManager
}
