package com.github.noamm9.packdisabler.config.impl

import com.github.noamm9.packdisabler.config.Config
import com.github.noamm9.packdisabler.config.types.ConfigSetting
import com.github.noamm9.packdisabler.config.types.ConfigSetting.Companion.deserializeEntries
import com.github.noamm9.packdisabler.config.types.ConfigSetting.Companion.serializeEntries
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ListSetting(
    override val name: String,
    initial: List<String> = deserializeEntries(Config.get(name).orEmpty())
): ArrayList<String>(initial), ConfigSetting<List<String>>, ReadOnlyProperty<Config, MutableList<String>> {
    override fun getValue(thisRef: Config, property: KProperty<*>): MutableList<String> = this
    override fun write() = serializeEntries(this)

    override fun add(element: String) = super.add(element).also { save() }
    override fun add(index: Int, element: String) = super.add(index, element).also { save() }
    override fun addAll(elements: Collection<String>) = super.addAll(elements).also { save() }
    override fun remove(element: String) = super.remove(element).also { save() }
    override fun clear() = super.clear().also { save() }
    override fun set(index: Int, element: String) = super.set(index, element).also { save() }
}