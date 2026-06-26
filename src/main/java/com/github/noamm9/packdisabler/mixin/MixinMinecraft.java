package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.MixinHooks;
import net.minecraft.client.Minecraft;
//? if >=26.2 {
/*import net.minecraft.client.gui.Gui;
*///?}
import net.minecraft.client.gui.screens.Overlay;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
/*@Mixin(Gui.class)
*///?} else {
@Mixin(Minecraft.class)
//?}
public class MixinMinecraft {
    @Inject(method = "setOverlay", at = @At("HEAD"), cancellable = true)
    private void onSetOverlay(@Nullable Overlay overlay, CallbackInfo ci) {
        MixinHooks.setOverlayHook(overlay, ci);
    }
}