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
        private fun overrideDyeColor(original: Int, itemStack: ItemStack): Int {
            val color = Config.armorColors[itemStack.uuid ?: return original] ?: return original
            return ARGB.opaque(color.toInt(16))
        }
    }
}