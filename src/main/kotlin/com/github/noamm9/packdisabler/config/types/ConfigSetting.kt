package com.github.noamm9.packdisabler.config.types

import com.github.noamm9.packdisabler.config.Config

interface ConfigSetting<T> {
    val name: String
    fun write(): String
    fun save() = Config.set(name, write())

    companion object {
        fun serializeEntries(entries: Collection<String>) = entries.joinToString(" ")
        fun deserializeEntries(raw: String): List<String> = if (raw.isEmpty()) emptyList() else raw.split(" ")
    }
}