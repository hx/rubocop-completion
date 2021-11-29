package com.github.hx.rubocopcompletion

import com.intellij.openapi.progress.ProgressIndicator
import java.nio.file.Path

class ChangeSetUpdater(private val changeSetsDir: Path, private val gemsDir: Path) {
    private val gemVersion = "0.1.6"
    private var installAttempted = false

    @Suppress("MagicNumber", "SwallowedException")
    fun run(progress: ProgressIndicator) {
        try {
            progress.isIndeterminate = true

            // Calls to this method should never overlap, but it never hurts to be thread-safe, right?
            synchronized(this) {
                if (!installAttempted) {
                    progress.fraction = 0.1
                    progress.text = "Ensuring required version ($gemVersion) of rubocop-schema-gen gem is installed …"
                    cmd("gem", "install", "rubocop-schema-gen", "--no-document", "--version", gemVersion)
                    installAttempted = true
                }
            }

            progress.fraction = 0.5
            progress.text = "Running RuboCop schema updater …"

            val genPath = gemsDir
                .resolve("bin")
                .resolve("rubocop-schema-gen")
                .toString()
            cmd(genPath, "_${gemVersion}_", "--build-repo=.")

            progress.isIndeterminate = false

            progress.fraction = 1.0
            progress.text = "RuboCop schema update complete"
            Thread.sleep(5_000)
        } catch (e: Command.Failed) {
            progress.isIndeterminate = false

            progress.fraction = 0.0
            progress.text = "RuboCop schema update failed; check idea.log for details"
            Thread.sleep(30_000)
        }
    }

    private fun cmd(vararg command: String) = Command(
        mapOf(
            "GEM_HOME" to gemsDir.toString(),
            "GEM_PATH" to gemsDir.toString()
        ),
        changeSetsDir,
        *command
    ).run()

    class Command(private val env: Map<String, String>, private val workDir: Path, vararg val command: String) {
        val builder = ProcessBuilder(*command).directory(workDir.toFile())!!

        init {
            for (x in env) {
                builder.environment()[x.key] = x.value
            }
        }

        class Failed(message: String) : Exception(message)

        fun run() {
            Logger.info(
                "Running in %s : %s %s".format(
                    workDir,
                    env.map { pair -> "${pair.key}=${pair.value}" }.joinToString(" "),
                    command.joinToString(" ")
                )
            )

            val process = builder.start()

            process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()

            process.waitFor()

            if (process.exitValue() != 0) {
                Logger.warn("Run failed: $error")
                throw Failed(error)
            }

            Logger.info("Run complete with no error")
        }
    }
}
