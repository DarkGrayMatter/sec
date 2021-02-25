package graymatter.sec.command.reuse.group

import graymatter.sec.App
import graymatter.sec.common.cli.CliValidationTarget
import graymatter.sec.common.io.StandardInputInputStream
import graymatter.sec.common.resourceAt
import picocli.CommandLine
import java.io.File
import java.io.InputStream

/**
 * Requirement to capture an input source for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class InputSourceArgGroup : CliValidationTarget {

    private data class InputFrom(val uri: String, val open: () -> InputStream)

    private var inputFrom: InputFrom? = null

    @CommandLine.Parameters(
        description = [
            "File to read from."
        ],
        paramLabel = "FILE"
    )
    fun setInputFile(file: File) {
        inputFrom = InputFrom(file.toURI().toString(), file::inputStream)
    }

    fun setInputStdIn(stdIn: Boolean) {
        if (stdIn) {
            inputFrom = InputFrom("stdin://", ::StandardInputInputStream)
        }
    }

    fun setInputFromClassPath(classPathResource: String) {
        inputFrom = InputFrom("classpath:/$classPathResource") {
            resourceAt<App>(classPathResource).openStream()
        }
    }


    val uri: String get() = requireNotNull(inputFrom).uri

    fun openInputStream() = requireNotNull(inputFrom).open()

    override fun validate(failWith: (error: String) -> String) {
        inputFrom ?: failWith("No input has been specified.")
    }
}

