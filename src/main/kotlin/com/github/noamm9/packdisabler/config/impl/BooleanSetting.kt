package com.github.noamm9.packdisabler.config.impl

import com.github.noamm9.packdisabler.config.Config
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BooleanSetting(private val key: String): ReadWriteProperty<Config, Boolean> {
    override fun getValue(thisRef: Config, property: KProperty<*>) = Config.get(key).toBoolean()
    override fun setValue(thisRef: Config, property: KProperty<*>, value: Boolean) = Config.set(key, value.toString())
}