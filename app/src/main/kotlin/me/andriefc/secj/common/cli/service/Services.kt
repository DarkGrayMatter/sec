package me.andriefc.secj.common.cli.service

import me.andriefc.secj.common.exception.CommandFailedException
import me.andriefc.secj.common.io.IOSource
import me.andriefc.secj.common.lang.tryAsKotlinSingleton
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
}

fun CommandLine.registerCommonExceptionMapping(): CommandLine {
    setExitCodeExceptionMapper { ex ->
        when (ex) {
            is CommandFailedException -> {
                System.err.println(ex.message)
                ex.exitCode
            }
            else -> CommandLine.ExitCode.SOFTWARE
        }
    }
    return this
}
