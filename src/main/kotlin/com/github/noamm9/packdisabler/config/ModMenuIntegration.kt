package com.github.noamm9.packdisabler.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint

@Entrypoint("modmenu")
class ModMenuIntegration: ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory(Config::createScreen)
    }
}