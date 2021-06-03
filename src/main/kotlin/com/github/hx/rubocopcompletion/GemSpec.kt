package com.github.hx.rubocopcompletion

/**
 * Specification (name/version) of an individual gem.
 */
data class GemSpec(val name: String, val version: String) {
    @Suppress("MagicNumber")
    private fun shortName(): String {
        if (name == "rubocop") {
            return name
        }
        return name.substring(8)
    }

    override fun toString(): String {
        return "${shortName()}-$version"
    }
}
