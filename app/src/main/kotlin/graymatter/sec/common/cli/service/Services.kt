package graymatter.sec.common.cli.service

import graymatter.sec.common.BinaryEncoding
import graymatter.sec.common.exception.CommandFailedException
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.io.IOSource
import graymatter.sec.common.lang.tryAsKotlinSingleton
import picocli.CommandLine
import picocli.CommandLine.IExitCodeExceptionMapper

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
    exitCodeExceptionMapper = IExitCodeExceptionMapper {
        (it as? CommandFailedException)?.exitCode ?: CommandLine.ExitCode.SOFTWARE
    }
    return this
}
