package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.MixinHooks.isLoading
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.screens.LoadingOverlay
import net.minecraft.client.gui.screens.Overlay
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

//? if <26.2 {
/*import net.minecraft.client.Minecraft

*///?}
//? if >=26.2 {
//?}
//? if >=26.2 {
@Mixin(Gui::class) //?} else {
/*@Mixin(Minecraft::class)

*///?}
class MixinMinecraft {
    @Inject(method = ["setOverlay"], at = [At("HEAD")], cancellable = true)
    private fun onSetOverlay(overlay: Overlay?, ci: CallbackInfo) {
        if (overlay is LoadingOverlay && isLoading) ci.cancel()
        else if (overlay == null && isLoading) isLoading = false
    }
}