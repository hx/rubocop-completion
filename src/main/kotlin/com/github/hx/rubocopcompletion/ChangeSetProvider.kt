package com.github.hx.rubocopcompletion

interface ChangeSetProvider {
    fun changeSetForGem(gemName: String): String?
}
