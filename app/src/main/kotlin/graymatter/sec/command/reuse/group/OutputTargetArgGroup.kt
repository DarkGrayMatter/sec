package graymatter.sec.command.reuse.group

import graymatter.sec.common.io.StandardOutputStream
import graymatter.sec.common.validation.ValidationTarget
import java.io.File
import java.io.OutputStream

/**
 * Requirement to capture an output target for a specific command.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class OutputTargetArgGroup : ValidationTarget {

    private var target: Target? = null

    fun setOutputToFile(file: File) {
        target = Target(file.toURI().toString(), file::outputStream)
    }

    fun setOutputToStdOut(stdOut: Boolean) {
        if (stdOut) {
            target = Target("stdout://") { StandardOutputStream() }
        }
    }

    override fun performValidation(validation: ValidationTarget.ValidationError) {
        target ?: validation.error("Please provide a suitable output target.")
    }

    private data class Target(val uri: String?, val open: () -> OutputStream)
}
