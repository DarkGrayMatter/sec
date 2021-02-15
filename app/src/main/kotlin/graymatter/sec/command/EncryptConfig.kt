package graymatter.sec.command

import graymatter.sec.command.mixins.ConfigOutputRequirements
import graymatter.sec.command.mixins.ConfigProcessingRulesRequirements
import graymatter.sec.command.mixins.ConfigSourceRequirements
import graymatter.sec.command.mixins.KeyRequirements
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin

@Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable {

    @Mixin
    lateinit var sourceRequirements: ConfigSourceRequirements

    @Mixin
    lateinit var keyKeyRequirements: KeyRequirements

    @Mixin
    lateinit var documentsProcessingRulesRequirements: ConfigProcessingRulesRequirements

    @Mixin
    lateinit var outputRequirements: ConfigOutputRequirements

    override fun run() {
        TODO("Not yet implemented")
    }

}
