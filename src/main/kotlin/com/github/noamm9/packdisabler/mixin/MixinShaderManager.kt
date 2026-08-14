package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.HypixelPackLoader
import com.github.noamm9.packdisabler.config.Config
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import net.minecraft.client.renderer.ShaderManager
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import java.util.function.*

@Mixin(ShaderManager::class)
abstract class MixinShaderManager {
    @WrapOperation(
        method = ["prepare"],
        at = [At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"
        )]
    )
    private fun selectTextColorResources(
        manager: ResourceManager,
        path: String,
        filter: Predicate<Identifier>,
        original: Operation<Map<Identifier, Resource>>
    ): Map<Identifier, Resource> {
        val resources = original.call(manager, path, filter)
        val locations = resources.keys.filter(::isTextColorResource)
        if (locations.isEmpty()) return resources

        val enabled = Config.newTextColors
        return resources.toMutableMap().apply {
            locations.forEach { location ->
                val selected = manager.getResourceStack(location).findLast {
                    (it.sourcePackId() == HypixelPackLoader.packId) == enabled
                }

                if (selected != null) this[location] = selected
                else if (! enabled) remove(location)
            }
        }
    }

    @Unique
    private fun isTextColorResource(location: Identifier): Boolean {
        if (location.namespace != "minecraft") return false
        return when (location.path) {
            "shaders/core/rendertype_text.vsh",
            "shaders/core/rendertype_text_see_through.vsh",
            "shaders/core/rendertype_text_intensity.vsh",
            "shaders/core/rendertype_text_intensity_see_through.vsh",
            "shaders/include/modify_vanilla_color.glsl",
            "shaders/include/color_util.glsl" -> true

            else -> false
        }
    }
}