package com.github.noamm9.packdisabler.mixin

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyVariable

@Mixin(GuiGraphicsExtractor::class)
abstract class MixinGuiGraphicsExtractor {
    @ModifyVariable(method = ["tooltip"], at = At("HEAD"), argsOnly = true)
    private fun onRenderTooltip(style: Identifier?): Identifier? {
        return if (style?.namespace == "hypixel_skyblock") null else style
    }
}