package me.andriefc.secj.common.cli

import me.andriefc.secj.common.lang.tryAsKotlinSingleton
import picocli.CommandLine


object CommandFactory : CommandLine.IFactory {
    private val default = CommandLine.defaultFactory()
    override fun <K : Any?> create(cls: Class<K>): K {
        return cls.tryAsKotlinSingleton() ?: default.create(cls)
    }
}
