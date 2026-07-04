package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.ResourceOverrides
import com.github.noamm9.packdisabler.config.Config
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.resources.FallbackResourceManager
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.util.function.*

@Mixin(FallbackResourceManager::class)
class MixinFallbackResourceManager {
    @Shadow @Final private val namespace: String? = null

    @Inject(method = ["push(Lnet/minecraft/server/packs/PackResources;)V"], at = [At("HEAD")], cancellable = true)
    private fun skipHypixelPack(pack: PackResources, ci: CallbackInfo) {
        skipHypixelPack(namespace !!, pack.packId(), ci)
    }

    @Inject(method = ["push(Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V"], at = [At("HEAD")], cancellable = true)
    private fun skipFilteredHypixelPack(pack: PackResources, filter: Predicate<Identifier?>?, ci: CallbackInfo) {
        skipHypixelPack(namespace !!, pack.packId(), ci)
    }

    @Inject(method = ["pushFilterOnly"], at = [At("HEAD")], cancellable = true)
    private fun skipHypixelPackFilter(packId: String, filter: Predicate<Identifier?>?, ci: CallbackInfo) {
        skipHypixelPack(namespace !!, packId, ci)
    }

    @Unique
    private fun skipHypixelPack(namespace: String, packId: String, ci: CallbackInfo) {
        if (! Config.disableGlobalPackOverrides) return
        if (namespace != Identifier.DEFAULT_NAMESPACE) return
        if (! ResourceOverrides.fromHypixelPack(packId)) return
        ci.cancel()
    }
}