package becode.sec

import becode.sec.command.DecryptValue
import becode.sec.command.EncryptValue
import becode.sec.command.GenerateKey
import becode.sec.command.GenerateRandom
import becode.sec.common.cli.service.CommandFactory
import becode.sec.common.cli.service.registerCommonConverters
import becode.sec.common.cli.service.registerExceptionHandlers
import picocli.CommandLine
import picocli.CommandLine.*
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
        GenerateRandom::class
        //EncryptConfigCommand::class,
    ]
)
object App {

    @JvmStatic
    fun main(args: Array<String>) {
        exitProcess(
            CommandLine(App, CommandFactory())
                .setExpandAtFiles(true)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setInterpolateVariables(true)
                .registerCommonConverters()
                .registerExceptionHandlers()
                .execute(* args)
        )
    }
}
