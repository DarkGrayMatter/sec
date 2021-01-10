package me.andriefc.secj

import me.andriefc.secj.comand.DecryptValue
import me.andriefc.secj.comand.EncryptValue
import me.andriefc.secj.comand.GenerateKeyPair
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
        GenerateKeyPair::class,
        EncryptValue::class,
        DecryptValue::class
    ]
)
object App {

    @JvmStatic
    fun main(args: Array<String>) {
        exitProcess(
            CommandLine(App, CommandFactory)
                .setExpandAtFiles(true)
                .separateOptionValuesWithSpace()
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(* args)
        )
    }

    private fun CommandLine.separateOptionValuesWithSpace() = setSeparator(SINGLE_SPACE)

    private const val SINGLE_SPACE = " "
}
