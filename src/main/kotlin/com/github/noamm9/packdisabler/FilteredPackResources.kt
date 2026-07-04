package com.github.noamm9.packdisabler

import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.IoSupplier
import java.io.InputStream

class FilteredPackResources(private val delegate: PackResources) : PackResources by delegate {
    override fun getResource(type: PackType, id: Identifier): IoSupplier<InputStream>? =
        if (id.path == FONT_PATH) delegate.getResource(type, id) else null

    override fun listResources(type: PackType, namespace: String, path: String, output: PackResources.ResourceOutput) =
        delegate.listResources(type, namespace, path) { id, resource ->
            if (id.path == FONT_PATH) output.accept(id, resource)
        }

    override fun close() = Unit

    private companion object {
        const val FONT_PATH = "font/default.json"
    }
}
