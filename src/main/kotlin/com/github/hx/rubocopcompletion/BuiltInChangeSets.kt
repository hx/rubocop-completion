package com.github.hx.rubocopcompletion

/**
 * Provides schema repos that are bundled with the plugin.
 */
object BuiltInChangeSets : ChangeSetProvider {
    /**
     * This list should always match exactly the contents of src/main/resources/change_sets
     */
    val knownGems = setOf(
        "rubocop",
        "rubocop-rake",
        "rubocop-rspec",
        "rubocop-minitest",
        "rubocop-performance",
        "rubocop-rails"
    )

    override fun changeSetForGem(gemName: String): String? {
        val stream = this::class.java.getResourceAsStream("/change_sets/$gemName.json") ?: return null
        return stream.reader().readText()
    }
}
