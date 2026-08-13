package com.github.noamm9.packdisabler.config.impl

import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.types.ConfigSetting
import com.github.noamm9.packdisabler.config.types.ConfigSetting.Companion.deserializeEntries
import com.github.noamm9.packdisabler.config.types.ConfigSetting.Companion.serializeEntries
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MapSetting(
    override val name: String,
    initial: Map<String, String> = deserializeEntries(Config.get(name).orEmpty()).associate { it.split("=", limit = 2).let { l -> Pair(l[0], l[1]) } }
): HashMap<String, String>(initial), ConfigSetting<Map<String, String>>, ReadOnlyProperty<Config, HashMap<String, String>> {
    override fun getValue(thisRef: Config, property: KProperty<*>): HashMap<String, String> = this
    override fun write() = serializeEntries(entries.map { "${it.key}=${it.value}" })

    override fun put(key: String, value: String): String? = super.put(key, value).also { save() }
    override fun putAll(from: Map<out String, String>) = super.putAll(from).also { save() }
    override fun remove(key: String): String? = super.remove(key).also { save() }
    override fun clear() = super.clear().also { save() }
}