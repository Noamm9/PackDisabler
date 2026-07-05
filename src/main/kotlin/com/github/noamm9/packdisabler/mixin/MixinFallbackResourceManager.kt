package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.FilteredPackResources
import com.github.noamm9.packdisabler.ResourceOverrides
import com.github.noamm9.packdisabler.config.Config
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.resources.FallbackResourceManager
import org.spongepowered.asm.mixin.Final
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyArgs
import org.spongepowered.asm.mixin.injection.invoke.arg.Args
import java.util.function.*

@Mixin(FallbackResourceManager::class)
class MixinFallbackResourceManager {
    @Shadow @Final private lateinit var namespace: String

    @ModifyArgs(
        method = [
            "pushFilterOnly",
            "push(Lnet/minecraft/server/packs/PackResources;)V",
            "push(Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V"
        ],
        at = At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/FallbackResourceManager;pushInternal(Ljava/lang/String;Lnet/minecraft/server/packs/PackResources;Ljava/util/function/Predicate;)V"
        )
    )
    private fun filterHypixelPack(args: Args) {
        if (! Config.disableGlobalPackOverrides) return
        if (namespace != Identifier.DEFAULT_NAMESPACE) return
        if (! ResourceOverrides.fromHypixelPack(args.get(0))) return

        val pack = args.get<PackResources?>(1)
        if (pack != null) args.set(1, FilteredPackResources(pack))
        else args.set(2, Predicate<Identifier> { false })
    }
}