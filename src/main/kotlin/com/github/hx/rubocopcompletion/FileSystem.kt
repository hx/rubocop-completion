package com.github.hx.rubocopcompletion

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileEvent
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.util.EventDispatcher

/**
 * The JSON schemas provided by this plugin are virtual files. This FileSystem class provides an
 * implementation of VirtualFileSystem that allows them to be consistently provided, and propagates
 * change events where necessary.
 *
 * The alternative is to use LightVirtualFile, which has some noticeable shortcomings around change
 * events that make it unsuitable.
 */
@Suppress("TooManyFunctions")
class FileSystem : VirtualFileSystem() {
    companion object {
        val instance = FileSystem()
    }

    private val dispatcher = EventDispatcher.create(VirtualFileListener::class.java)
    private val schemas = HashMap<SchemaSpec, Schema>()

    /**
     * Given a schema specification, should provide a schema file to match.
     */
    fun schemaForSpec(spec: SchemaSpec): Schema {
        synchronized(schemas) {
            if (!schemas.containsKey(spec)) {
                schemas[spec] = Schema(this, spec)
            }
            return schemas[spec]!!
        }
    }

    internal fun schemaIsReady(schema: Schema) {
        val virtualFileEvent = VirtualFileEvent(null, schema, null, 0, 1)
        dispatcher.multicaster.beforeContentsChange(virtualFileEvent)
        dispatcher.multicaster.contentsChanged(virtualFileEvent)
    }

    override fun getProtocol() = "rubocop-schema"

    override fun findFileByPath(p0: String): VirtualFile? = null
    override fun refresh(p0: Boolean) { /* no-op */ }
    override fun refreshAndFindFileByPath(p0: String): VirtualFile? = null

    override fun addVirtualFileListener(listener: VirtualFileListener) = dispatcher.addListener(listener)
    override fun removeVirtualFileListener(listener: VirtualFileListener) = dispatcher.removeListener(listener)

    override fun deleteFile(p0: Any?, p1: VirtualFile) = throw unsupported("deleteFile")
    override fun moveFile(p0: Any?, p1: VirtualFile, p2: VirtualFile) = throw unsupported("moveFile")
    override fun renameFile(p0: Any?, p1: VirtualFile, p2: String) = throw unsupported("renameFile")
    override fun createChildFile(p0: Any?, p1: VirtualFile, p2: String) = throw unsupported("createChildFile")
    override fun createChildDirectory(p0: Any?, p1: VirtualFile, p2: String) = throw unsupported("createChildDirectory")
    override fun copyFile(p0: Any?, p1: VirtualFile, p2: VirtualFile, p3: String) = throw unsupported("copyFile")

    override fun isReadOnly(): Boolean = true

    private fun unsupported(op: String): UnsupportedOperationException {
        return UnsupportedOperationException("${this::class.qualifiedName} cannot perform $op")
    }
}
