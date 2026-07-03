package com.github.noamm9.packdisabler.mixin;

import com.github.noamm9.packdisabler.FilteredPackResources;
import com.github.noamm9.packdisabler.MixinHooks;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Predicate;

@Mixin(FallbackResourceManager.class)
public class MixinFallbackResourceManager {
    @Shadow @Final private String namespace;

    @ModifyArgs(
        method = {
            "push(Lnet/minecraft/server/packs/PackResources;)V",
            "push(Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V",
            "pushFilterOnly"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;pushInternal(Ljava/lang/String;Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V"
        )
    )
    private void filterHypixelPack(Args args) {
        if (!MixinHooks.shouldFilterHypixelPack(namespace, args.get(0))) return;

        PackResources pack = args.get(1);
        if (pack != null) args.set(1, new FilteredPackResources(pack));
        else args.set(2, (Predicate<Identifier>) id -> false);
    }
}