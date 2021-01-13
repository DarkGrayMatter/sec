package me.andriefc.secj.core.cli

import me.andriefc.secj.core.lang.tryAsKotlinSingleton
import picocli.CommandLine

object CommandFactory : CommandLine.IFactory {
    private val default = CommandLine.defaultFactory()
    override fun <K : Any?> create(cls: Class<K>): K {
        return cls.tryAsKotlinSingleton() ?: default.create(cls)
    }
}
