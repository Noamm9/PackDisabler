package com.github.noamm9.packdisabler

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint

@Entrypoint(Entrypoint.PRE_LAUNCH)
class PackDisablerPreLaunch: PreLaunchEntrypoint {
    override fun onPreLaunch() {
        HypixelPackLoader.initialPack.start()
    }
}