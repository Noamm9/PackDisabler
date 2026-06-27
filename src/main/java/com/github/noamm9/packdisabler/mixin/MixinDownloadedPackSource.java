package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.MixinHooks;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.resources.server.PackReloadConfig;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DownloadedPackSource.class)
public class MixinDownloadedPackSource {
    @Inject(method = "loadRequestedPacks", at = @At("RETURN"))
    private void onResourcePacksReady(List<PackReloadConfig.IdAndPath> requestedPacks, CallbackInfoReturnable<List<Pack>> cir) {
        MixinHooks.resourcePacksReadyHook(cir.getReturnValue());
    }
}
