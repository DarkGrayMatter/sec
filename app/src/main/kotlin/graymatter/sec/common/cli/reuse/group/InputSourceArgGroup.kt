package graymatter.sec.common.cli.reuse.group

import graymatter.sec.common.io.IOSource
import picocli.CommandLine
import java.io.File

/**
 * Requirement to capture an input source for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class InputSourceArgGroup {

    lateinit var source: IOSource.Input
        private set

    @CommandLine.Parameters(
        description = ["Supply a file as input."],
        paramLabel = "FILE-IN"
    )
    fun setInputFile(file: File) {
        source = IOSource.Input.File(file)
    }

    @CommandLine.Option(
        names = ["--stdin"],
        description = ["Use STDIN as input."],
        defaultValue = "false"
    )
    fun setStdInput(standardInput: Boolean) {
        if (standardInput) {
            source = IOSource.Input.StdIn()
        }
    }

}

