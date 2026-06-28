package com.github.noamm9.packdisabler

import java.util.*
import java.util.concurrent.*

object ResourceOverrides {
    private val serverPackIdPattern = Regex("""server/[0-9A-F]{8}/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})""")
    private val hypixelPackIds = ConcurrentHashMap.newKeySet<String>()

    fun addPack(id: UUID) {
        hypixelPackIds += id.toString()
    }

    fun removePack(id: UUID?): Boolean {
        if (id != null) return hypixelPackIds.remove(id.toString())
        return hypixelPackIds.isNotEmpty().also { clear() }
    }

    fun clear() = hypixelPackIds.clear()

    @JvmStatic
    fun fromHypixelPack(packId: String): Boolean {
        val id = serverPackIdPattern.matchEntire(packId)?.groupValues?.get(1) ?: return false
        return id in hypixelPackIds
    }
}