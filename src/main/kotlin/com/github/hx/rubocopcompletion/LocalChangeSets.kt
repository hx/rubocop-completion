package com.github.hx.rubocopcompletion

import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.exists
import com.intellij.util.io.isDirectory
import com.intellij.util.io.readText
import com.intellij.util.io.write
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.TimeUnit

/**
 * This is a change set on the local file system.
 */
class LocalChangeSets(private val directory: Path) : ChangeSetProvider {
    companion object {
        const val checkForUpdatesEvery: Long = 3 // Hours

        val userHome = LocalChangeSets(
            Paths.get(System.getProperty("user.home")).resolve(".rubocop-configuration-repo")
        )
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
            Logger.error("LocalChangeSets expected a directory but got a file")
        } else {
            for (gemName in BuiltInChangeSets.knownGems) {
                if (!hasChangeSetForGem(gemName)) {
                    pathForGem(gemName).write(BuiltInChangeSets.changeSetForGem(gemName)!!)
                }
            }
            AppExecutorUtil
                .getAppScheduledExecutorService()
                .scheduleWithFixedDelay({ checkForUpdates() }, 0, checkForUpdatesEvery, TimeUnit.HOURS)
        }
    }

    private fun checkForUpdates() {
        runBackgroundableTask("Looking for new RuboCop docs") { progress ->
            ChangeSetUpdater(directory).run(progress)
        }
    }

    override fun changeSetForGem(gemName: String): String? {
        if (!directory.exists() || !hasChangeSetForGem(gemName)) {
            return null
        }
        return pathForGem(gemName).readText()
    }

    private fun pathForGem(gemName: String) = directory.resolve("$gemName.json")
    private fun hasChangeSetForGem(gemName: String) = pathForGem(gemName).exists()
}
