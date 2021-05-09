package graymatter.sec.command.reuse.group

import graymatter.sec.common.io.StandardOutputStream
import picocli.CommandLine.Option
import java.io.File
import java.io.OutputStream

/**
 * Requirement to capture an output target for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class OutputTargetProvider  {

    val isFile: Boolean
        get() = target is Target.File

    val isStdOut: Boolean
        get() = target is Target.StdOut

    private var target: Target? = null

    @Option(
        names = ["--file-out"],
        description = ["Output to a specific file."]
    )
    fun setOutputToFile(file: File) {
        target = Target.File(file)
    }

    @Option(
        names = ["--stdout"],
        description = ["Output to standard out instead of file."],
        required = true,
        defaultValue = "true"
    )
    fun setOutputToStdOut(stdOut: Boolean) {
        if (stdOut && !isAvailable) {
            target = Target.StdOut()
        }
    }

    fun setOutputToStdOut() = setOutputToStdOut(true)

    val uri: String? get() = target?.uri
    val isAvailable: Boolean get() = target != null

    fun openOutputStream(): OutputStream {
        return requireNotNull(target).open()
    }

    private sealed class Target(val uri: String?, val open: () -> OutputStream) {
        class File(file: java.io.File) : Target(file.toURI().toString(), file::outputStream)
        class StdOut() : Target(null, { StandardOutputStream() })
    }

}
