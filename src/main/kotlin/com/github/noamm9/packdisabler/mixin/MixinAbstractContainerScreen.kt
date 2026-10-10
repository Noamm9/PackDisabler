package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.Utils.chat
import com.github.noamm9.packdisabler.Utils.rawNBT
import com.github.noamm9.packdisabler.config.managers.WLM
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.KeyEvent
import net.minecraft.world.inventory.Slot
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(AbstractContainerScreen::class)
abstract class MixinAbstractContainerScreen {
    @Shadow @JvmField protected var hoveredSlot: Slot? = null

    @Inject(method = ["keyPressed"], at = [At("HEAD")], cancellable = true)
    private fun onKeyPressed(event: KeyEvent, cir: CallbackInfoReturnable<Boolean>) {
        val stack = hoveredSlot?.takeIf(Slot::hasItem)?.item ?: return
        when {
            WLM.debugKeybind.matches(event) -> {
                Minecraft.getInstance().keyboardHandler.clipboard = stack.rawNBT
                chat("Copied raw item data to clipboard.")
            }
            WLM.keybind.matches(event) -> WLM.id(stack)?.let(WLM::toggle)
            else -> return
        }
        cir.returnValue = true
    }
}