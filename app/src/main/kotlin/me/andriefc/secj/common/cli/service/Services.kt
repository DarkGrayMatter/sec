package me.andriefc.secj.common.cli

import me.andriefc.secj.common.IOSource
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
fun CommandLine.registerAppConverters(): CommandLine = apply {
    registerConverter(IOSource.Output::class.java, IOSource.Output.Companion::fromString)
    registerConverter(IOSource.Input::class.java, IOSource.Input.Companion::fromString)
}
