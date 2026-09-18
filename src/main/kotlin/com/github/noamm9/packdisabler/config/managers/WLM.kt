package com.github.noamm9.packdisabler.config.managers

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.Utils.customData
import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.config.Config
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.world.item.ItemStack

object WLM {
    val keybind = KeyMapping("PackDisabler Whitelist Item", InputConstants.KEY_P, KeyMapping.Category.INVENTORY)

    fun toggle(id: String) {
        if (id in Config.whitelist) {
            Config.whitelist.remove(id)
            chat("Removed §e$id§r from whitelist!")
        }
        else {
            Config.whitelist.add(id)
            chat("Added §e$id§r to whitelist!")
        }
    }

    // could be expended in the future
    fun id(stack: ItemStack?): String? {
        if (stack == null) return null
        if (stack.isEmpty) return null

        val data = stack.customData
        if ("quiver_arrow" in data) return "quiver_arrow"

        return skyblockId(data)
    }
}