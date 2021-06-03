package com.github.hx.rubocopcompletion

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.util.io.*
import java.lang.RuntimeException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions

/**
 * This is a change set on the local file system.
 */
class LocalChangeSets(private val directory: Path) : ChangeSetProvider {
    companion object {
        val userHome = LocalChangeSets(Paths.get(System.getProperty("user.home")).resolve(".rubocop-configuration-repo"))
    }

    init {
        if (!directory.exists()) {
            Files.createDirectories(
                    directory,
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwxr-xr-x")
                    )
            )
        } else if (!directory.isDirectory()) {
            throw RuntimeException("expected a directory but got a file")
        }
        for (gemName in BuiltInChangeSets.knownGems) {
            if (!hasChangeSetForGem(gemName)) {
                pathForGem(gemName).write(BuiltInChangeSets.changeSetForGem(gemName)!!)
            }
        }
        runBackgroundableTask("Looking for new RuboCop docs") { progress ->
            ChangeSetUpdater(directory).run(progress)
        }
    }

    override fun changeSetForGem(gemName: String): String? {
        if (!directory.exists()) {
            return null
        }
        if (hasChangeSetForGem(gemName)) {
            return pathForGem(gemName).readText()
        }
        return null
    }

    private fun pathForGem(gemName: String) = directory.resolve("$gemName.json")
    private fun hasChangeSetForGem(gemName: String) = pathForGem(gemName).exists()
}
