package graymatter.sec

import graymatter.sec.command.*
import graymatter.sec.common.cli.CommandFactory
import graymatter.sec.common.cli.ToolVersionProvider
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.exception.CommandFailedException
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.HelpCommand
import kotlin.system.exitProcess

@Command(
    name = "sec",
    description = ["SEC is a configuration companion to the excellent Palantir library."],
    versionProvider = ToolVersionProvider::class,
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
        exitProcess(createCommandLine().execute(* args))
    }

    fun createCommandLine(cmd: Any = this): CommandLine{
        return CommandLine(cmd, CommandFactory)
            .registerExceptionHandlers()
            .setExpandAtFiles(true)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setInterpolateVariables(true)
            .registerCommonConverters()
            .setUsageHelpWidth(150)
    }


    private fun CommandLine.registerCommonConverters(): CommandLine = apply {
        registerConverter(BinaryEncoding::class.java, BinaryEncoding.Companion::fromName)
    }

    private fun CommandLine.registerExceptionHandlers(): CommandLine {
        exitCodeExceptionMapper = CommandLine.IExitCodeExceptionMapper {
            (it as? CommandFailedException)?.exitCode ?: CommandLine.ExitCode.SOFTWARE
        }
        return this
    }

}
