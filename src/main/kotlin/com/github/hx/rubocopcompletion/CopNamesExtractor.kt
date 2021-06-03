package com.github.hx.rubocopcompletion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object CopNamesExtractor {
    private val departmentDescription = Regex(""" department(?: \(.+ extension\))?$""")

    /**
     * Parses the schema's JsonObject for names of cops, and prepares a list of completions.
     */
    internal fun extract(schema: JsonObject): List<LookupElement> {
        for (entry in schema) {
            if (entry.key == "properties") {
                return extractFromProperties(entry.value as JsonObject)
            }
        }
        return listOf()
    }

    private fun extractFromProperties(properties: JsonObject): List<LookupElement> {
        val entries = mutableListOf<LookupElement>()
        for (property in properties) {
            val name = property.key
            if (isQualifiedCopName(name) && !isDepartment(property.value as JsonObject)) {
                entries.add(LookupElementBuilder.create(name).withIcon(AllIcons.Json.Object))
            }
        }
        return entries
    }

    private fun isQualifiedCopName(name: String): Boolean {
        return name[0].isUpperCase() && name != "AllCops"
    }

    private fun isDepartment(property: JsonObject): Boolean {
        val description = property["description"]
        return description is JsonPrimitive && departmentDescription.containsMatchIn(description.content)
    }
}
