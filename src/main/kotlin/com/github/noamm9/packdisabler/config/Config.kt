package com.github.noamm9.packdisabler.config

import com.github.noamm9.packdisabler.config.impl.ListSetting
import com.github.noamm9.packdisabler.config.impl.MapSetting
import com.github.noamm9.packdisabler.config.impl.StringSetting
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.util.*

// this was fun to make ngl, It's nice to use something else then json
object Config {
    private val configDir = FabricLoader.getInstance().configDir.resolve("@MODID@").toFile()
    private val configFile = configDir.resolve("@MODID@.properties")
    private val config = Properties().apply {
        configFile.takeIf(File::exists)?.reader()?.use(::load) ?: run {
            configDir.mkdirs()
            configFile.createNewFile()
        }
    }

    var packUrl by StringSetting("packUrl")
    val whitelist by ListSetting("whitelist")
    val replacements by MapSetting("replacements")

    fun get(key: String): String? = config.getProperty(key)
    fun set(key: String, value: String) = config.setProperty(key, value).let { save() }
    private fun save() = configFile.outputStream().use { config.store(it, "PackDisabler config") }
}