package me.andriefc.secj

import me.andriefc.secj.comand.DecryptValueCommand
import me.andriefc.secj.comand.EncryptConfigFileCommand
import me.andriefc.secj.comand.EncryptValueCommand
import me.andriefc.secj.comand.GenerateKeyPairCommand
import me.andriefc.secj.commons.cli.CommandFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand
import kotlin.system.exitProcess

@Command(
    name = "sec",
    description = ["Security companion to the excellent Palantar library."],
    mixinStandardHelpOptions = true,
    subcommands = [
        HelpCommand::class,
        GenerateKeyPairCommand::class,
        EncryptValueCommand::class,
        DecryptValueCommand::class,
        EncryptConfigFileCommand::class
    ]
)
object App {

    @JvmStatic
    fun main(args: Array<String>) {
        exitProcess(
            CommandLine(App, CommandFactory)
                .setExpandAtFiles(true)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setInterpolateVariables(true)
                .execute(* args)
        )
    }
}
