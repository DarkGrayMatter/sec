package graymatter.sec.command

import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.common.validation.ValidationTarget
import picocli.CommandLine
import picocli.CommandLine.ArgGroup

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable, ValidationTarget {

    @ArgGroup(
        exclusive = true,
        order = 1,
        heading = "Choose any of the following unencrypted configuration sources :%n"
    )
    lateinit var configInput: InputSourceArgGroup

    @ArgGroup(
        exclusive = true,
        order = 2,
        heading = "Choose any of the following method to output the encrypted document:%n"
    )
    lateinit var configOutput: OutputTargetArgGroup

    override fun run() {
    }

    override fun performValidation(validation: ValidationTarget.ValidationError) {
        configInput.performValidation(validation)
        configOutput.performValidation(validation)
    }


}

