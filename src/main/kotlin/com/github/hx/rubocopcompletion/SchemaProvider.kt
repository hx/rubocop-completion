package com.github.hx.rubocopcompletion

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider

/**
 * This singleton is responsible for supplying RuboCop configuration schemas customised for
 * each individual project. It also listens to projects for file changes, and invalidates
 * its cache when necessary.
 */
object SchemaProvider : ModuleSchemaRepo {
    /**
     * This class receives file change notifications, and invalidates cache entries with
     * the changes files as their keys.
     */
    private class ChangeDelegate(val schemasByLockFile: HashMap<VirtualFile, Schema>) : BulkFileListener {
        override fun after(events: MutableList<out VFileEvent>) {
            for (e in events) {
                synchronized(schemasByLockFile) { schemasByLockFile.remove(e.file) }
            }
        }
    }

    /**
     * Keys are Gemfile.lock files. Values are ready-to-serve JSON schema files.
     */
    private val schemasByLockFile = HashMap<VirtualFile, Schema>()

    /**
     * Tracks the projects for which SchemaProvider has listeners, so we only add
     * one per project.
     */
    private val projectListeners: MutableSet<Project> = mutableSetOf()

    /**
     * The instance of ChangeDelegate that will receive file change notifications.
     */
    private val changeDelegate = ChangeDelegate(schemasByLockFile)

    /**
     * Main entrypoint into the provider. Returns one ModuleSchemaProvider for each
     * module in the given project. Ruby projects typically only have one module.
     */
    fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        subscribeToProject(project)

        return ModuleManager.getInstance(project).modules
                .filter { findLockFileForModule(it) != null }
                .map { ModuleSchemaProvider(it, this) }
    }

    /**
     * Implements ModuleSchemaRepo. Given a module, provides a schema if one can be
     * formed for the module.
     */
    override fun getSchemaForModule(module: Module): Schema? {
        val lockFile = findLockFileForModule(module) ?: return null
        synchronized(schemasByLockFile) {
            if (!schemasByLockFile.containsKey(lockFile)) {
                val spec = SchemaSpec.fromLockFile(lockFile) ?: return null
                schemasByLockFile[lockFile] = FileSystem.instance.schemaForSpec(spec)
            }
            return schemasByLockFile[lockFile]
        }
    }

    private fun subscribeToProject(project: Project) {
        synchronized(projectListeners) {
            if(!projectListeners.contains(project)) {
                project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, changeDelegate)
            }
        }
    }

    private fun findLockFileForModule(module: Module): VirtualFile? {
        for (root in module.rootManager.contentRoots) {
            for (file in root.children) {
                if (file.name == "Gemfile.lock") {
                    return file
                }
            }
        }
        return null
    }
}
