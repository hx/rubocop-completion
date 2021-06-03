package com.github.hx.rubocopcompletion

import com.intellij.openapi.vfs.VirtualFile

/**
 * Represents all relevant gem specs that can comprise a single schema. Typically formed by
 * parsing a Gemfile.lock file using fromLockFile().
 */
class SchemaSpec(gemSpecs: List<GemSpec>) {
    companion object {
        private val specPattern = Regex("""^\s+(rubocop(?:-\w+)?)\s+\((\d+(?:\.\d+)*)\)\s*$""")

        fun fromLockFile(lockFile: VirtualFile): SchemaSpec? {
            val specs = mutableListOf<GemSpec>()
            for (line in lockFile.inputStream.bufferedReader().lines()) {
                val match = specPattern.matchEntire(line) ?: continue
                if (BuiltInChangeSets.knownGems.contains(match.groupValues[1])) {
                    specs.add(GemSpec(match.groupValues[1], match.groupValues[2]))
                }
            }
            if (specs.isNotEmpty()) {
                return SchemaSpec(specs)
            }
            return null
        }
    }

    val gemSpecs: List<GemSpec> = gemSpecs.sortedBy { it.name }

    override fun toString() = gemSpecs.joinToString("-")
    override fun hashCode() = toString().hashCode() + 99
    override fun equals(other: Any?) = other is SchemaSpec && other.toString() == toString()
}
