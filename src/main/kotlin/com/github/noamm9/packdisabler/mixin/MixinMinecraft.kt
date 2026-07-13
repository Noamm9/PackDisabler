package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.HypixelPackLoader
import net.minecraft.client.Minecraft
import net.minecraft.server.packs.repository.RepositorySource
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.ModifyArg

@Mixin(Minecraft::class)
abstract class MixinMinecraft {
    @ModifyArg(
        method = ["<init>"],
        at = At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/repository/PackRepository;<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"
        ),
        index = 0
    )
    private fun addHypixelPackSource(repositorySources: Array<RepositorySource>): Array<RepositorySource> {
        return repositorySources + HypixelPackLoader.HypixelPackRepositorySource()
    }
}