package com.github.noamm9.packdisabler.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Entrypoint("modmenu")
@Environment(EnvType.CLIENT)
class ModMenuIntegration: ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory(Config::createScreen)
    }
}