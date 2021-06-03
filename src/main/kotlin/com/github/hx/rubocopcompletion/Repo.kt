package com.github.hx.rubocopcompletion

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

class Repo(private val provider: ChangeSetProvider) {
    companion object {
        val builtIn = Repo(BuiltInChangeSets)
        val userHome = Repo(LocalChangeSets.userHome)
    }

    @Serializable
    private data class Entry(val version: String, val diff: JsonObject)

    private val entryLists = HashMap<String, List<Entry>>()

    fun schemaForSpec(schemaSpec: SchemaSpec): JsonObject {
        var result = JsonObject(mapOf())
        for (gemSpec in schemaSpec.gemSpecs) {
            result = applyDiffToHash(result, compileEntries(gemSpec))
        }
        return result
    }

    private fun compileEntries(gemSpec: GemSpec): JsonObject {
        var result = JsonObject(mapOf())
        for (entry in fetchEntries(gemSpec.name)) {
            result = applyDiffToHash(result, entry.diff)
            if (entry.version == gemSpec.version) { // TODO: use semver comparison
                break
            }
        }
        return result
    }

    private fun applyDiff(old: JsonElement, new: JsonElement): JsonElement {
        if (old is JsonObject && new is JsonObject) {
            return applyDiffToHash(old, new)
        }
        return new
    }

    private fun applyDiffToHash(target: JsonObject, diff: JsonObject): JsonObject {
        val result = target.toMutableMap()
        for (key in diff.keys) {
            val value = diff[key]!!
            when {
                value is JsonNull -> result.remove(key)
                result.containsKey(key) -> result[key] = applyDiff(result[key]!!, value)
                else -> result[key] = value
            }
        }
        return JsonObject(result)
    }

    private fun fetchEntries(gemName: String): List<Entry> {
        synchronized(entryLists) {
            if (!entryLists.containsKey(gemName)) {
                val json = provider.changeSetForGem(gemName) ?: return listOf()
                entryLists[gemName] = Json.decodeFromString(json)
            }
        }
        return entryLists[gemName]!!
    }
}
