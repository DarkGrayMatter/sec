package graymatter.sec

import graymatter.sec.command.*
import graymatter.sec.common.cli.service.CommandFactory
import graymatter.sec.common.cli.service.registerCommonConverters
import graymatter.sec.common.cli.service.registerExceptionHandlers
import picocli.CommandLine
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
            CommandLine(App, CommandFactory())
                .registerExceptionHandlers()
                .setExpandAtFiles(true)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setInterpolateVariables(true)
                .registerCommonConverters()
                .setUsageHelpWidth(150)
                .setUsageHelpAutoWidth(true)
                .execute(* args)
        )
    }
}
