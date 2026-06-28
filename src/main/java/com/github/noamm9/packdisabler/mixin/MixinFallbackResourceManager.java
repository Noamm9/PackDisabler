package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.MixinHooks;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(FallbackResourceManager.class)
public class MixinFallbackResourceManager {
    @Shadow @Final private String namespace;

    @Inject(method = "push(Lnet/minecraft/server/packs/PackResources;)V", at = @At("HEAD"), cancellable = true)
    private void skipHypixelPack(PackResources pack, CallbackInfo ci) {
        MixinHooks.skipHypixelPack(namespace, pack.packId(), ci);
    }

    @Inject(method = "push(Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V", at = @At("HEAD"), cancellable = true)
    private void skipFilteredHypixelPack(PackResources pack, Predicate<Identifier> filter, CallbackInfo ci) {
        MixinHooks.skipHypixelPack(namespace, pack.packId(), ci);
    }

    @Inject(method = "pushFilterOnly", at = @At("HEAD"), cancellable = true)
    private void skipHypixelPackFilter(String packId, Predicate<Identifier> filter, CallbackInfo ci) {
        MixinHooks.skipHypixelPack(namespace, packId, ci);
    }
}