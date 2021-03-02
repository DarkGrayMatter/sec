package graymatter.sec.common.cli

import graymatter.sec.common.validation.DefaultValidator
import graymatter.sec.common.validation.ValidationTarget
import picocli.CommandLine

fun CommandLine.Model.CommandSpec.validate(target: ValidationTarget) {

    val validations =
        DefaultValidator()
            .apply { with(target) { validate() } }
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
