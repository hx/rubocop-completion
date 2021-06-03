package com.github.hx.rubocopcompletion

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Represents a schema for RuboCop configuration files, based on a SchemaSpec. Implements VirtualFile, enabling
 * it to be passed directly to the editor for consumption. Also provides auto-completions for the names of cops
 * in its properties.
 */
@Suppress("TooManyFunctions")
class Schema(private val fs: FileSystem, private val spec: SchemaSpec) : VirtualFile() {
    // TODO: if IntelliJ ever actually respond to the events raised by FileSystem.schemaIsReady,
    //   use this placeholder, do schema generation asynchronously, and call it.
    //
    // """{"${"$"}schema":"http://json-schema.org/draft-07/schema","type":"object"}${"\n"}""".toByteArray()

    private var contents: ByteArray

    val copNameCompletions: List<LookupElement>

    init {
        Logger.info("Generating schema $spec")
        val schema = Repo.userHome.schemaForSpec(spec)
        contents = schema.toString().toByteArray()
        Logger.info("Generated schema is ${contents.size} bytes")
        copNameCompletions = CopNamesExtractor.extract(schema)
    }

    override fun getModificationStamp(): Long = 0
    override fun getName() = "$spec.json"
    override fun getFileSystem() = fs
    override fun getPath() = "/$name"
    override fun isWritable() = false
    override fun isDirectory() = false
    override fun isValid() = true
    override fun getParent() = null
    override fun getChildren() = null

    override fun getOutputStream(p0: Any?, p1: Long, p2: Long): OutputStream {
        return object : ByteArrayOutputStream() {
            override fun close() {
                contents = toByteArray()
            }
        }
    }

    override fun contentsToByteArray() = contents
    override fun getTimeStamp() = 0.toLong()
    override fun getLength() = contentsToByteArray().size.toLong()
    override fun refresh(p0: Boolean, p1: Boolean, p2: Runnable?) { /* no-op */ }
    override fun getInputStream() = VfsUtilCore.byteStreamSkippingBOM(contentsToByteArray(), this)
}
