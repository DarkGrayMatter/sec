package me.andriefc.secj.common.exception

import picocli.CommandLine

sealed class CommonToolException(message: String) : RuntimeException(message)

class CommandFailedException(
    userMessage: String,
    val exitCode: Int
) : CommonToolException(userMessage)


fun fail(exitCode: Int, message: String): Nothing = throw CommandFailedException(message, exitCode)
fun fail(message: String): Nothing = fail(CommandLine.ExitCode.SOFTWARE, message)
