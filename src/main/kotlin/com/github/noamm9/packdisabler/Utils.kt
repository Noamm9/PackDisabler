package com.github.noamm9.packdisabler

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.jvm.optionals.getOrNull

object Utils {
    val ItemStack.customData get() = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
    val ItemStack.itemUUID get() = customData.getString("uuid").getOrNull() ?: ""
    val ItemStack.skyblockId get() = if (!isEmpty) customData.getString("id").getOrNull()?.replace(":", "-").orEmpty() else ""
}