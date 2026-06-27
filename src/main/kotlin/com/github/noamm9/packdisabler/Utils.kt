package com.github.noamm9.packdisabler

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.jvm.optionals.getOrNull

object Utils {
    val ItemStack.customData get() = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
    val ItemStack.skyblockId get() = skyblockId(customData)

    val skyblockId = fun(tag: CompoundTag) = tag.getString("id").getOrNull()?.replace(":", "-")
}