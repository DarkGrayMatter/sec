package graymatter.sec.common.cli

import graymatter.sec.common.validation.DefaultValidator
import graymatter.sec.common.validation.ValidationTarget
import graymatter.sec.common.validation.Validator
import picocli.CommandLine

fun CommandLine.Model.CommandSpec.verify(target: ValidationTarget) {

    val validations =
        DefaultValidator()
            .also(target::validate)
            .takeIf { !it.passed() }
            ?.failures()
            ?: return

    val commandline = commandLine()
    val command = commandline.commandName
    val userMessage = validations.withIndex().joinToString(
        prefix = "\nUnable to process \"${command}\". The following errors were reported:\n\n",
        separator = "\n",
        postfix = "\n\nFor your convenience please review the usage for \"$command\"\n"
    ) { (index, hint) -> "${index + 1}. $hint" }

    throw CommandLine.ParameterException(commandline, userMessage)
}

fun CommandLine.Model.CommandSpec.verify(action: Validator.() -> Unit) {
    verify(ValidationTarget { it.action() })
}
