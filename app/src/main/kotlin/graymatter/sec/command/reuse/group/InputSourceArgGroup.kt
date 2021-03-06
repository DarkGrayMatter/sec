package graymatter.sec.command.reuse.group

import graymatter.sec.App
import graymatter.sec.common.io.StandardInputInputStream
import graymatter.sec.common.resourceAt
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.io.InputStream

/**
 * Requirement to capture an input source for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class InputSourceArgGroup {

    private data class Target(val uri: String?, val open: () -> InputStream)

    val isSupplied: Boolean get() = target != null

    private var target: Target? = null
    var isStdIn: Boolean = false; private set

    @Parameters(
        description = [
            "File to read from."
        ],
        paramLabel = "FILE_IN"
    )
    fun setInputFile(file: File) {
        target = Target(file.toURI().toString(), file::inputStream)
    }

    @Option(
        names = ["--stdin"],
        description = [
            "Read from STDIN"
        ]
    )
    fun setInputStdIn(stdIn: Boolean) {
        if (stdIn) {
            target = Target(null, ::StandardInputInputStream)
        }
    }

    @Option(
        names = ["--input-res"],
        required = true,
        description = ["Read input from a resource on the classpath."]
    )
    fun setInputFromClassPath(classPathResource: String) {
        target = Target("classpath:/$classPathResource") {
            resourceAt<App>(classPathResource).openStream()
        }
    }

    val uri: String? get() = target?.uri

    fun openInputStream() = requireNotNull(target).open()

}

