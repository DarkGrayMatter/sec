package me.andriefc.secj.common.exception

import picocli.CommandLine

sealed class CommonToolException(override val message: String) : RuntimeException(message)

class CommandFailedException(
    userMessage: String,
    val exitCode: Int
) : CommonToolException(userMessage)


fun failCommand(exitCode: Int, message: String): Nothing = throw CommandFailedException(message, exitCode)
fun failCommand(message: String): Nothing = failCommand(CommandLine.ExitCode.SOFTWARE, message)
