package graymatter.sec.common.cli

import graymatter.sec.common.validation.Validation
import graymatter.sec.common.validation.Validator
import picocli.CommandLine
import picocli.CommandLine.Model.CommandSpec


@CommandLine.Command
abstract class SelfValidatingCommand : Runnable {

    @set:CommandLine.Spec
    lateinit var cli: CommandSpec

    final override fun run() {
        applyDefaults()
        validateSelf()
        performAction()
    }

    private fun validateSelf() {
        when (val failedValidations = Validator().apply { validateSelf() }.takeIf { it.hasFailures() }) {
            null -> performAction()
            else -> {
                if (!this::cli.isInitialized) {
                    cli = CommandSpec.forAnnotatedObject(this)
                }
                cli.process(failedValidations.failures())
            }
        }
    }


    protected abstract fun Validator.validateSelf()
    protected abstract fun performAction()
    protected open fun applyDefaults() = Unit

    companion object {

        private fun CommandSpec.process(failures: List<Validation.Failure>) {

            if (failures.isEmpty()) {
                return
            }

            val commandLine: CommandLine? = commandLine()
            val command = commandLine?.commandName ?: "command line"
            val userMessage = buildString {
                appendLine()
                appendLine("Unable to process $command. The following errors were reported:")
                failures.withIndex().joinTo(this, "\n") { (index, error) -> "\t${index + 1}. $error" }
                appendLine()
                appendLine("For your assistance, please consult the usage below ↩")
                appendLine()
            }

            throw CommandLine.ParameterException(commandLine, userMessage)
        }
    }
}
