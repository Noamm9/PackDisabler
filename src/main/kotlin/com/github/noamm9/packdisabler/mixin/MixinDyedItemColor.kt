package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.Utils.uuid
import com.github.noamm9.packdisabler.config.Config
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import net.minecraft.util.ARGB
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.DyedItemColor
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At

@Mixin(DyedItemColor::class)
abstract class MixinDyedItemColor {
    private companion object {
        @JvmStatic
        @ModifyReturnValue(method = ["getOrDefault"], at = [At("RETURN")])
        private fun overrideDyeColor(original: Int, stack: ItemStack): Int {
            val dyes = Config.armorDyes
            if (dyes.isEmpty()) return original

            val rgb = dyes[stack.uuid]?.toIntOrNull(16) ?: return original
            return if (rgb in 0..0xFFFFFF) ARGB.opaque(rgb) else original
        }
    }
}
