package com.github.noamm9.packdisabler.config

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object ReplacementConfig {
    private val gson = Gson()

    fun encode(replacements: Map<String, String>): String =
        Base64.getEncoder().encodeToString(compress(gson.toJson(replacements)))

    fun decode(input: String): Map<String, String>? = runCatching {
        val json = if (input.startsWith("{")) input else decompress(Base64.getDecoder().decode(input))
        JsonParser.parseString(json).asJsonObject.entrySet().associate { (target, replacement) ->
            target to replacement.asString
        }
    }.getOrNull()

    private fun compress(input: String): ByteArray = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { it.write(input.toByteArray(Charsets.UTF_8)) }
        output.toByteArray()
    }

    private fun decompress(input: ByteArray): String = GZIPInputStream(input.inputStream()).use { gzip ->
        gzip.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
