package com.github.noamm9.packdisabler.mixin

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.component.CustomData
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(CustomData::class)
interface ICustomData {
    @get:Accessor("tag")
    val tag: CompoundTag
}