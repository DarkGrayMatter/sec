package graymatter.sec.common.cli

import graymatter.sec.common.validation.DefaultValidationContext
import graymatter.sec.common.validation.ValidationTarget
import graymatter.sec.common.validation.ValidationContext
import picocli.CommandLine

fun CommandLine.Model.CommandSpec.validate(target: ValidationTarget) {

    val validations =
        DefaultValidationContext()
            .also(target::validate)
            .takeIf { !it.passed() }
            ?.failures()
            ?: return

    val commandline = commandLine()
    val command = commandline.commandName
    val userMessage = validations.withIndex().joinToString(
        prefix = "\nUnable to process \"${command}\". The following errors were reported:\n\n",
        separator = "\n",
        postfix = "\n\nFor your assistance please consult the usage below:\n"
    ) { (index, hint) -> "${index + 1}. $hint" }

    throw CommandLine.ParameterException(commandline, userMessage)
}

fun validate(commandSpec: CommandLine.Model.CommandSpec, action: ValidationContext.() -> Unit) {
    commandSpec.validate { it.action() }
}
