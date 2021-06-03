package com.github.hx.rubocopcompletion

import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

/**
 * This is IntelliJ's entrypoint into the plugin as far as providing schemas is concerned.
 * The ProviderFactory directive in plugin.xml tells it to make one of these, and call its
 * getProviders() function. We return the SchemaProvider singleton, since we want things
 * memoized for the life of the process.
 */
class SchemaProviderFactory : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): MutableList<JsonSchemaFileProvider> {
        return SchemaProvider.getProviders(project).toMutableList()
    }
}
