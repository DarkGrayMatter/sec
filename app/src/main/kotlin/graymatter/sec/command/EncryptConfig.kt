package graymatter.sec.command

import picocli.CommandLine
import picocli.CommandLine.Model.CommandSpec

@CommandLine.Command(name = "encrypt-config", description = ["Encrypt a configuration document given a key"])
class EncryptConfig : Runnable {

    @CommandLine.Spec
    lateinit var spec: CommandSpec




    override fun run() {
    }


}

