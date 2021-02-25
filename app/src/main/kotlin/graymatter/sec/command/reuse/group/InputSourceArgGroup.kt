package graymatter.sec.command.reuse.group

import graymatter.sec.App
import graymatter.sec.common.io.StandardInputInputStream
import graymatter.sec.common.resourceAt
import graymatter.sec.common.validation.ValidationTarget
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.io.InputStream

/**
 * Requirement to capture an input source for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class InputSourceArgGroup : ValidationTarget {

    private data class Target(val uri: String?, val open: () -> InputStream)

    private var target: Target? = null

    @Parameters(
        description = [
            "File to read from."
        ],
        paramLabel = "FILE"
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

    @Option(names = ["--input-resource"])
    fun setInputFromClassPath(classPathResource: String) {
        target = Target("classpath:/$classPathResource") {
            resourceAt<App>(classPathResource).openStream()
        }
    }

    val uri: String? get() = requireNotNull(target).uri

    fun openInputStream() = requireNotNull(target).open()

    override fun performValidation(validation: ValidationTarget.ValidationError) {
        target ?: validation.error("Please provide input.")
    }
}

