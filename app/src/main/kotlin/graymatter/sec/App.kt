package graymatter.sec

import graymatter.sec.AppConfig.createCommandLine
import graymatter.sec.command.*
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand
import kotlin.system.exitProcess

@Command(
    name = "sec",
    description = ["SEC is a configuration companion to the excellent Palantir library."],
    mixinStandardHelpOptions = true,
    subcommands = [
        HelpCommand::class,
        GenerateKey::class,
        EncryptValue::class,
        DecryptValue::class,
        GenerateRandomBytes::class,
        EncryptConfig::class
    ]
)
object App {
    @JvmStatic
    fun main(args: Array<String>) {
        exitProcess(
            createCommandLine(App).execute(* args)
        )
    }
}
