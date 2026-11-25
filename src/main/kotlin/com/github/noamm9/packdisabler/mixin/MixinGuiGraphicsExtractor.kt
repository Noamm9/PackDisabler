package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.HypixelPackLoader
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(GuiGraphicsExtractor::class)
abstract class MixinGuiGraphicsExtractor {
    @ModifyVariable(method = ["tooltip"], at = At("HEAD"), argsOnly = true)
    private fun onRenderTooltip(style: Identifier?): Identifier? {
        return if (style?.namespace == HypixelPackLoader.packId) null else style
    }
}