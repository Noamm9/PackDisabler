package com.github.noamm9.packdisabler.config.impl

import com.github.noamm9.packdisabler.config.Config
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StringSetting(private val key: String): ReadWriteProperty<Config, String?> {
    override fun getValue(thisRef: Config, property: KProperty<*>) = Config.get(key)?.ifEmpty { null }
    override fun setValue(thisRef: Config, property: KProperty<*>, value: String?) = Config.set(key, value.orEmpty())
}