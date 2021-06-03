package com.github.hx.rubocopcompletion

import com.intellij.openapi.progress.ProgressIndicator
import java.nio.file.Path

class ChangeSetUpdater(private val directory: Path) {
    private val gemVersion = "0.1.4"

    fun run(progress: ProgressIndicator) {
        try {
            progress.isIndeterminate = true

            progress.fraction = 0.1
            progress.text = "Installing required version ($gemVersion) of rubocop-schema-gen gem …"
            cmd("gem", "install", "rubocop-schema-gen", "--no-document", "--version", gemVersion)

            progress.fraction = 0.5
            progress.text = "Running updater …"
            cmd("rubocop-schema-gen", "_${gemVersion}_", "--build-repo=.")

            progress.isIndeterminate = false

            progress.fraction = 1.0
            progress.text = "Completed successfully"
            Thread.sleep(5_000)
        } catch(e: Command.Failed) {
            progress.isIndeterminate = false

            progress.fraction = 0.0
            progress.text = "Update failed; check idea.log for details"
            Thread.sleep(30_000)
        }
    }

    private fun cmd(vararg command: String) = Command(directory, *command).run()

    class Command(private val directory: Path, vararg val command: String) {
        val builder = ProcessBuilder(*command).directory(directory.toFile())!!

        class Failed(message: String) : Exception(message)

        fun run() {
            Logger.info("Running in %s : %s".format(directory, command.joinToString(" ")))

            try {
                val process = builder.start()

                process.inputStream.bufferedReader().readText()
                val error = process.errorStream.bufferedReader().readText()

                process.waitFor()

                if (process.exitValue() != 0) {
                    throw Failed(error)
                }
                Logger.info("Run complete with no error")
            } catch (e: Exception) {
                Logger.warn("Run failed: %s: %s".format(e.javaClass.name, e.message))
                throw Failed(e.javaClass.name + ": "+e.message)
            }
        }
    }
}
