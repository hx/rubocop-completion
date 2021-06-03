package com.github.hx.rubocopcompletion

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion

/**
 * Implements JsonSchemaFileProvider. These are the things given to IntelliJ by SchemaProviderFactory when
 * it asks for providers. We provide one for each module, though Ruby projects typically only have one
 * module. They are built by SchemaProvider, using itself as a repo.
 */
class ModuleSchemaProvider(private val module: Module, private val repo: ModuleSchemaRepo) : JsonSchemaFileProvider {
    override fun isAvailable(file: VirtualFile) =
        file.fileType.name == "YAML" &&
            file.name.startsWith(".rubocop") &&
            module.rootManager.fileIndex.isInContent(file) &&
            repo.getSchemaForModule(module) != null

    override fun getName() = schemaFile?.nameWithoutExtension ?: "rubocop.yml"
    override fun getSchemaFile() = repo.getSchemaForModule(module)
    override fun getSchemaType() = SchemaType.embeddedSchema
    override fun getSchemaVersion() = JsonSchemaVersion.SCHEMA_7
}
