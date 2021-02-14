package graymatter.sec.command

import graymatter.sec.common.cli.mixins.ConfigOutputRequirements
import graymatter.sec.common.cli.mixins.ConfigSourceRequirements
import graymatter.sec.common.cli.mixins.ConfigProcessingRulesRequirements
import graymatter.sec.common.cli.mixins.KeyRequirements
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin

@Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable {

    @Mixin
    lateinit var sourceRequirements: ConfigSourceRequirements
        internal set

    @Mixin
    lateinit var keyKeyRequirements: KeyRequirements
        internal set

    @Mixin
    lateinit var documentsProcessingRulesRequirements: ConfigProcessingRulesRequirements
        internal set

    @Mixin
    lateinit var outputRequirements: ConfigOutputRequirements
        internal set

    override fun run() {
        TODO("Not yet implemented")
    }


}
