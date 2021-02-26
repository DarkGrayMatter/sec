package graymatter.sec.command.parts

import picocli.CommandLine.Option

/**
 * A mixin to expose a `--dryrun', or `--noop` switch
 */
class DryRun {

    @Option(
        names = ["--dryrun", "--noop"],
        defaultValue = "false",
        description = [
            "Do not really perform \"\${COMMAND-NAME}\". Only prints out information on parameters/configuration."
        ]
    )
    var enabled: Boolean = false

}
