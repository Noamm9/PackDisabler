package com.github.noamm9.packdisabler.mixin

import com.github.noamm9.packdisabler.HypixelPackLoader
import com.github.noamm9.packdisabler.config.Config
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.FallbackResourceManager
import net.minecraft.server.packs.resources.Resource
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.*

@Mixin(FallbackResourceManager::class)
abstract class MixinFallbackResourceManager {
    @Shadow abstract fun getResourceStack(location: Identifier): List<Resource>

    @Inject(method = ["listResources"], at = [At("RETURN")], cancellable = true)
    private fun selectTextColorResources(path: String, filter: Predicate<Identifier>, cir: CallbackInfoReturnable<Map<Identifier, Resource>>) {
        val resources = cir.returnValue.toMutableMap()
        resources.keys.filter(::isTextColorResource).forEach { location ->
            val stack = getResourceStack(location)
            val selected = if (Config.newTextColors) {
                stack.find { it.sourcePackId() == HypixelPackLoader.packId } ?: stack.lastOrNull()
            }
            else stack.findLast { it.sourcePackId() != HypixelPackLoader.packId }

            if (selected == null) resources.remove(location)
            else resources[location] = selected
        }

        cir.returnValue = resources
    }

    @Unique
    private fun isTextColorResource(location: Identifier): Boolean {
        if (location.namespace != "minecraft") return false
        return when (location.path) {
            "shaders/core/text.vsh",
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