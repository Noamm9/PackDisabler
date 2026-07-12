package com.github.noamm9.packdisabler.config

import com.github.noamm9.packdisabler.Utils.chat
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object WhitelistManager {
    val keybind = KeyMapping("PackDisabler Whitelist Item", GLFW.GLFW_KEY_P, KeyMapping.Category.INVENTORY)

    fun toggle(sbid: String) {
        if (sbid in Config.whitelist) {
            Config.whitelist.remove(sbid)
            chat("Removed §e$sbid§r from whitelist!")
        }
        else {
            Config.whitelist.add(sbid)
            chat("Added §e$sbid§r to whitelist!")
        }
    }
}