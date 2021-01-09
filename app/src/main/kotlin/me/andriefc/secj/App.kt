package me.andriefc.secj

import me.andriefc.secj.comand.DecryptValue
import me.andriefc.secj.comand.EncryptValue
import me.andriefc.secj.comand.GenerateKeyPair
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand
import kotlin.system.exitProcess

@Command(
    name = "secj",
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
            CommandLine(App)
                .setAtFileCommentChar('@')
                .setExpandAtFiles(true)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(* args)
        )
    }
}
