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

    @Option(
        names = ["--file-out"],
        description = ["Output to a specific file."]
    )
    fun setOutputToFile(file: File) {
        target = Target(file.toURI().toString(), file::outputStream)
    }

    @Option(
        names = ["--stdout"],
        description = ["Output to standard out instead of file."],
        required = true,
        defaultValue = "true"
    )
    fun setOutputToStdOut(stdOut: Boolean) {
        if (stdOut) {
            target = Target("stdout://") { StandardOutputStream() }
        }
    }

    fun setOutputToStdOut() = setOutputToStdOut(true)

    val uri: String? get() = target?.uri
    val isAvailable: Boolean get() = target != null
    fun openOutputStream(): OutputStream {
        return requireNotNull(target).open()
    }

    private data class Target(val uri: String?, val open: () -> OutputStream)
}
