package graymatter.sec.command.reuse.mixin

import graymatter.sec.common.document.DocumentFormat
import picocli.CommandLine
import java.time.LocalDateTime

/**
 * These requirements gives control over the format of report, and how its named.
 *
 * @see [nameReport]
 */
class ReportingRequirements {

    @CommandLine.Spec
    private lateinit var spec: CommandLine.Model.CommandSpec

    @CommandLine.Option(
        names = ["--reporting"],
        defaultValue = "false",
        required = false,
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
        description = [
            "Enable/Disable the generation of reporting data for \${COMMAND-NAME}"
        ]
    )
    var reporting: Boolean = false

    @CommandLine.Option(
        names = ["--report-format"],
        required = false,
        defaultValue = "json",
        description = [
            "The format of the reporting data."
        ]
    )
    var reportFormat: DocumentFormat = DocumentFormat.JSON

    @CommandLine.Option(
        names = ["--report-pretty-print"],
        defaultValue = "false",
        required = false,
        description = [
            "Produce pretty print data (where applicable to the format)."
        ]
    )
    var prettyPrint: Boolean = true

    @CommandLine.Option(
        names = ["--report-identifier"],
        defaultValue = "report",
        description = [
            "Part of the suffix used to indicate that file contains reporting data, for example \"-report.json\"."
        ]
    )
    var reportNameIdentifier: String = "report"

    @CommandLine.Option(
        names = ["--report-identifier-timestamped"],
        negatable = true,
        showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
        defaultValue = "true",
        description = [
            "Switch to indicate that a timestamp should be used when generating a report suffix."
        ]
    )
    var reportIdentifierTimestamped: Boolean = true

    @CommandLine.Option(
        names = ["--report-name"],
        description = [
            "Name of the report being generated. Defaults to name of the command.",
            "Feel free to change this name if it not suited."
        ]
    )
    lateinit var reportName: String

    /**
     * Name this report based on the options set by via the command line.
     */
    fun nameReport(): String {
        return buildString {
            append(reportName)
            join(SUFFIX_SEPARATOR, reportNameIdentifier)
            if (reportIdentifierTimestamped) {
                join(SUFFIX_SEPARATOR, LocalDateTime.now().toString())
            }
            join(FILENAME_EXT, reportFormat.fileExtension)
        }
    }

    companion object {
        private const val SUFFIX_SEPARATOR = '-'
        private const val FILENAME_EXT = '.'
        private fun StringBuilder.join(separator: Char, part: String) {
            if (isNotEmpty() && last() != separator) append(separator)
            append(part)
        }
    }
}
