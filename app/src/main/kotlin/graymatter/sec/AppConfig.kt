package graymatter.sec

import graymatter.sec.common.BinaryEncoding
import graymatter.sec.common.cli.CommandFactory
import graymatter.sec.common.exception.CommandFailedException
import graymatter.sec.common.io.IOSource
import picocli.CommandLine
import picocli.CommandLine.IExitCodeExceptionMapper

object AppConfig {

    fun createCommandLine(): CommandLine {
        return CommandLine(App, CommandFactory)
            .registerExceptionHandlers()
            .setExpandAtFiles(true)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setInterpolateVariables(true)
            .registerCommonConverters()
            .setUsageHelpWidth(150)
            .setUsageHelpAutoWidth(true)
    }

    private fun CommandLine.registerCommonConverters(): CommandLine = apply {
        registerConverter(IOSource.Output::class.java, IOSource.Output.Companion::fromString)
        registerConverter(IOSource.Input::class.java, IOSource.Input.Companion::fromString)
        registerConverter(BinaryEncoding::class.java, BinaryEncoding.Companion::fromName)
    }

    private fun CommandLine.registerExceptionHandlers(): CommandLine {
        exitCodeExceptionMapper = IExitCodeExceptionMapper {
            (it as? CommandFailedException)?.exitCode ?: CommandLine.ExitCode.SOFTWARE
        }
        return this
    }

}
