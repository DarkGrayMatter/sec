package graymatter.sec.common.cli.service

import becode.sec.common.BinaryEncoding
import becode.sec.common.exception.CommandFailedException
import becode.sec.common.exception.failCommand
import becode.sec.common.io.IOSource
import becode.sec.common.lang.tryAsKotlinSingleton
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
