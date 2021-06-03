package com.github.hx.rubocopcompletion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Parses the schema's JsonObject for names of cops, and prepares a list of completions.
 */
internal fun extractCopNames(schema: JsonObject): List<LookupElement> {
    val departmentDescription = Regex(""" department(?: \(.+ extension\))?$""")
    val entries = mutableListOf<LookupElement>()
    for (entry in schema) {
        if (entry.key == "properties") {
            for (property in entry.value as JsonObject) {
                val name = property.key
                if (!name[0].isUpperCase() || name == "AllCops") {
                    continue
                }
                val description = (property.value as JsonObject)["description"]
                if (description is JsonPrimitive && departmentDescription.containsMatchIn(description.content)) {
                    continue
                }
                entries.add(LookupElementBuilder.create(name))
            }
        }
    }
    return entries
}
