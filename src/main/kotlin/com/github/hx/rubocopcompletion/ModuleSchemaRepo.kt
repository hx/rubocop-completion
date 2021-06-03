package com.github.hx.rubocopcompletion

import com.intellij.openapi.module.Module

interface ModuleSchemaRepo {
    fun getSchemaForModule(module: Module): Schema?
}
