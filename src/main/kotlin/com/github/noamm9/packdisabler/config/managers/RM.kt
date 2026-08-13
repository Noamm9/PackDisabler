package com.github.noamm9.packdisabler.config.managers

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.util.zip.*
import kotlin.io.encoding.Base64

object RM {
    private val gson = Gson()

    fun encode(replacements: Map<String, String>) = Base64.encode(compress(gson.toJson(replacements)))

    fun decode(input: String): Map<String, String>? = runCatching {
        val json = if (input.startsWith("{")) input else decompress(Base64.decode(input))
        JsonParser.parseString(json).asJsonObject.entrySet().associate { (target, replacement) ->
            target to replacement.asString
        }
    }.getOrNull()


    private fun compress(input: String) = ByteArrayOutputStream().use { output ->
        GZIPOutputStream(output).use { it.write(input.toByteArray(Charsets.UTF_8)) }
        output.toByteArray()
    }

    private fun decompress(input: ByteArray) = GZIPInputStream(input.inputStream()).use { gzip ->
        gzip.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}