package com.github.noamm9.packdisabler

import net.minecraft.client.Minecraft
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData
import kotlin.jvm.optionals.getOrNull

object Utils {
    val ItemStack.customData get() = getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
    val ItemStack.skyblockId get() = skyblockId(customData)
    val ItemStack.skyblockSkinId get() = normalizedId(customData, "skin")

    val skyblockId = fun(tag: CompoundTag) = normalizedId(tag, "id")

    @JvmField
    val chat = fun(msg: String) {
        val component = prefix.copy().append(Component.literal(" $msg"))
        val mc = Minecraft.getInstance()

        /*? if =26.1.2 { */
        /*mc.gui.chat.addClientSystemMessage(component)
        *//*? } else if =1.21.11 { */
        /*mc.gui.chat.addMessage(component)
        *//*? } else if =26.2 { */
        mc.gui.hud.chat.addClientSystemMessage(component)
        /*? } */
    }

    private val normalizedId = fun(tag: CompoundTag, key: String) = tag.getString(key).getOrNull()?.replace(":", "-")

    private val prefix = Component.empty().apply {
        append(Component.literal("[").withColor(0x4498DB))
        append(Component.literal("P").withColor(0x5091D6))
        append(Component.literal("a").withColor(0x5D8AD0))
        append(Component.literal("c").withColor(0x6982CB))
        append(Component.literal("k").withColor(0x757BC6))
        append(Component.literal("D").withColor(0x8274C0))
        append(Component.literal("i").withColor(0x8E6DBB))
        append(Component.literal("s").withColor(0x9A65B6))
        append(Component.literal("a").withColor(0xA65EB1))
        append(Component.literal("b").withColor(0xB357AB))
        append(Component.literal("l").withColor(0xBF50A6))
        append(Component.literal("e").withColor(0xCB48A1))
        append(Component.literal("r").withColor(0xD8419B))
        append(Component.literal("]").withColor(0xE43A96))
    }
}