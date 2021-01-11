package me.andriefc.secj.commons.cli

import me.andriefc.secj.commons.lang.tryAsKotlinSingleton
import picocli.CommandLine


object CommandFactory : CommandLine.IFactory {
    private val default = CommandLine.defaultFactory()
    override fun <K : Any?> create(cls: Class<K>): K {
        return cls.tryAsKotlinSingleton() ?: default.create(cls)
    }
}
