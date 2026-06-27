package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.ResourceOverrides;
import com.github.noamm9.packdisabler.config.Config;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
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
        if (shouldSkip(pack.packId())) ci.cancel();
    }

    @Inject(method = "push(Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V", at = @At("HEAD"), cancellable = true)
    private void skipFilteredHypixelPack(PackResources pack, Predicate<Identifier> filter, CallbackInfo ci) {
        if (shouldSkip(pack.packId())) ci.cancel();
    }

    @Inject(method = "pushFilterOnly", at = @At("HEAD"), cancellable = true)
    private void skipHypixelPackFilter(String packId, Predicate<Identifier> filter, CallbackInfo ci) {
        if (shouldSkip(packId)) ci.cancel();
    }

    private boolean shouldSkip(String packId) {
        return namespace.equals(Identifier.DEFAULT_NAMESPACE) &&
            Config.Companion.getINSTANCE().getDisableGlobalPackOverrides() &&
            ResourceOverrides.INSTANCE.belongsToHypixelPack(packId);
    }
}
