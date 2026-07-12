package com.github.noamm9.packdisabler.config

import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.util.*

// this was fun to make ngl, It's nice to use something else then json
object Config {
    private val configDir = FabricLoader.getInstance().configDir.resolve("@MODID@").toFile()
    private val configFile = configDir.resolve("@MODID@.properties")
    private val config = Properties().apply {
        configFile.takeIf(File::exists)?.reader()?.use(::load)
    }

    fun get(key: String): String? = config.getProperty(key)
    fun set(key: String, value: String) = config.setProperty(key, value).also { save() }

    val whitelist get() = PersistedList(get("whitelist")?.takeUnless(String::isEmpty)?.split(" ") ?: emptyList())

    private fun save() {
        configFile.parentFile.mkdirs()
        configFile.outputStream().use { config.store(it, "PackDisabler config") }
    }

    class PersistedList(initial: List<String>): ArrayList<String>(initial) {
        private fun persist() = this@Config.set("whitelist", joinToString(" "))

        override fun add(element: String) = super.add(element).also { persist() }
        override fun add(index: Int, element: String) = super.add(index, element).also { persist() }

        override fun addAll(elements: Collection<String>) = super.addAll(elements).also { persist() }
        override fun remove(element: String) = super.remove(element).also { persist() }
        override fun removeAt(index: Int) = super.removeAt(index).also { persist() }
        override fun clear() = super.clear().also { persist() }

        override fun set(index: Int, element: String) = super.set(index, element).also { persist() }
    }
}