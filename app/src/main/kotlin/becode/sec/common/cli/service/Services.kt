package becode.sec.common.cli.service

import becode.sec.common.BinaryEncoding
import becode.sec.common.exception.CommandFailedException
import becode.sec.common.exception.failCommand
import becode.sec.common.io.IOSource
import becode.sec.common.lang.tryAsKotlinSingleton
import picocli.CommandLine

class CommandFactory : CommandLine.IFactory {
    private val default = CommandLine.defaultFactory()
    override fun <K : Any?> create(cls: Class<K>): K {
        return cls.tryAsKotlinSingleton() ?: default.create(cls)
    }
}

/**
 * Register all application value converters.
 */
fun CommandLine.registerCommonConverters(): CommandLine = apply {
    registerConverter(IOSource.Output::class.java, IOSource.Output.Companion::fromString)
    registerConverter(IOSource.Input::class.java, IOSource.Input.Companion::fromString)
    registerConverter(BinaryEncoding::class.java, BinaryEncoding.Companion::fromName)
}

/**
 * Customize the command line's exception handling to correctly print out messages
 * and exit codes passed via the [failCommand]
 *
 * @see CommandFailedException.message
 * @see CommandFailedException.exitCode
 */
fun CommandLine.registerExceptionHandlers(): CommandLine {

    object : CommandLine.IExitCodeExceptionMapper, CommandLine.IExecutionExceptionHandler {

        private val defaultHandler = executionExceptionHandler

        init {
            executionExceptionHandler = this
            exitCodeExceptionMapper = this
        }

        override fun getExitCode(exception: Throwable): Int {
            return (exception as? CommandFailedException)?.exitCode ?: CommandLine.ExitCode.SOFTWARE
        }

        override fun handleExecutionException(
            exception: Exception,
            commandLine: CommandLine,
            parseResult: CommandLine.ParseResult
        ): Int {
            return when (exception) {
                is CommandFailedException -> {
                    reportErrorToUser(parseResult, exception)
                    exception.exitCode
                }
                else -> defaultHandler.handleExecutionException(exception, commandLine, parseResult)
            }
        }

        private fun reportErrorToUser(
            parseResult: CommandLine.ParseResult,
            exception: CommandFailedException
        ) {
            val source = parseResult.subcommand().commandSpec().name()
            System.err.println("[error:$source] ${exception.message}")
        }
    }

    return this
}
