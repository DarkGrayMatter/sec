package graymatter.sec.command.reuse.group

import graymatter.sec.common.io.StandardOutputStream
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.io.OutputStream

/**
 * Requirement to capture an output target for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class OutputTargetArgGroup  {

    private var target: Target? = null

    @Parameters(
        paramLabel = "FILE_OUT",
        description = ["File to output to."],
        arity = "1"
    )
    fun setOutputToFile(file: File) {
        target = Target(file.toURI().toString(), file::outputStream)
    }

    @Option(
        names = ["--stdout"],
        description = ["Output to standard out instead of file."],
        required = true
    )
    fun setOutputToStdOut(stdOut: Boolean) {
        if (stdOut) {
            target = Target("stdout://") { StandardOutputStream() }
        }
    }

    val uri: String? get() = target?.uri

    fun openOutput(): OutputStream {
        return requireNotNull(target).open()
    }

    private data class Target(val uri: String?, val open: () -> OutputStream)
}
