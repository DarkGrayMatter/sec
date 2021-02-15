package graymatter.sec.command

import graymatter.sec.command.mixins.*
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

    @Mixin
    lateinit var reportingRequirements: ReportingRequirements

    override fun run() {
        TODO("Not yet implemented")
    }

}
