package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.MixinHooks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import java.util.List;

//? if =1.21.11 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}

//? if =1.21.11 {
/*@Mixin(GuiGraphics.class)
*///?} else {
@Mixin(GuiGraphicsExtractor.class)
//?}
public class MixinGuiGraphicsExtractor {

    //? if = 1.21.11 {
    /*@WrapMethod(method = "renderTooltip")
     *///?} else {
    @WrapMethod(method = "tooltip")
        //?}
    private void onRenderTooltip(Font font, List<ClientTooltipComponent> lines, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier style, Operation<Void> original) {
        MixinHooks.renderToolTipHook(font, lines, x, y, positioner, style, original);
    }
}