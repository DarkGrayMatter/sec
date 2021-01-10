package me.andriefc.secj.commons.cli

import me.andriefc.secj.commons.lang.asKotlinObject
import picocli.CommandLine
import picocli.CommandLine.defaultFactory

object CommandFactory : CommandLine.IFactory {
    override fun <K : Any?> create(cls: Class<K>): K {
        return cls.asKotlinObject() ?: defaultFactory().create(cls)
    }
}
