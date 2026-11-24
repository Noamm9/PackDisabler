package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.config.Config;
import com.github.noamm9.packdisabler.config.managers.WLM;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import kotlin.text.StringsKt;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedItemColor.class)
abstract class MixinDyedItemColor {
    @ModifyReturnValue(method = "getOrDefault", at = @At("RETURN"))
    private static int overrideDyeColor(int original, ItemStack stack) {
        var dyes = Config.INSTANCE.getArmorDyes();
        if (dyes.isEmpty()) return original;

        String hex = dyes.get(WLM.INSTANCE.id(stack));
        if (hex == null) return original;

        Integer rgb = StringsKt.toIntOrNull(hex, 16);
        return rgb != null && rgb >= 0 && rgb <= 0xFFFFFF ? ARGB.opaque(rgb) : original;
    }
}
