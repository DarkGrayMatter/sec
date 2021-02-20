package graymatter.sec.common.cli.reuse.group

import graymatter.sec.common.io.IOSource
import picocli.CommandLine
import java.io.File

/**
 * Requirement to capture an output target for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class OutputTargetArgGroup {

    lateinit var output: IOSource.Output
        private set

    @CommandLine.Parameters(
        paramLabel = "FILE-OUT",
        description = [
            "Use a file as input source."
        ]
    )
    fun setOutputFile(file: File) {
        output = IOSource.Output.File(file)
    }

    @CommandLine.Option(
        hidden = true,
        names = ["--drop-output"]
    )
    fun setNullFile(dropOutput: Boolean) {
        if (dropOutput) {
            output = IOSource.Output.NULL
        }
    }

    @CommandLine.Option(
        names = ["--stdout"],
        defaultValue = "false",
        description = ["Output should go to STDOUT."]
    )
    fun setStdOut(isStdOut: Boolean) {
        if (isStdOut) {
            output = IOSource.Output.StdOut()
        }
    }

}
