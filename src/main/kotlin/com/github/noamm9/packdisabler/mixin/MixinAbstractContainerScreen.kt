package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.Utils.skyblockId
import com.github.noamm9.packdisabler.config.WhitelistManager
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
    @Shadow protected var hoveredSlot: Slot? = null

    @Inject(method = ["keyPressed"], at = [At("HEAD")])
    private fun onKeyPressed(event: KeyEvent, cir: CallbackInfoReturnable<Boolean>) {
        if (! WhitelistManager.keybind.matches(event)) return
        val slot = hoveredSlot?.takeIf(Slot::hasItem) ?: return
        val sbid = slot.item.skyblockId ?: return
        WhitelistManager.toggle(sbid)
    }
}