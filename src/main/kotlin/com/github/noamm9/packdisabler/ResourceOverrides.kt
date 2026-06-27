package com.github.noamm9.packdisabler

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object ResourceOverrides {
    private val serverPackIdPattern = Regex("""server/[0-9A-F]{8}/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})""")
    private val hypixelPackIds = ConcurrentHashMap.newKeySet<String>()
    fun updatePack(id: UUID, isHypixelPack: Boolean): Boolean {
        if (isHypixelPack) {
            hypixelPackIds += id.toString()
            return false
        }
        return hypixelPackIds.remove(id.toString())
    }

    fun removePack(id: UUID?): Boolean {
        if (id != null) return hypixelPackIds.remove(id.toString())

        val removedHypixelPack = hypixelPackIds.isNotEmpty()
        clear()
        return removedHypixelPack
    }

    fun clear() = hypixelPackIds.clear()

    fun belongsToHypixelPack(packId: String): Boolean {
        val id = serverPackIdPattern.matchEntire(packId)?.groupValues?.get(1) ?: return false
        return id in hypixelPackIds
    }
}
