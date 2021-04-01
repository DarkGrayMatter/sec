package graymatter.sec.command

import graymatter.sec.command.reuse.group.OutputTargetProvider
import graymatter.sec.command.reuse.mixin.GivenSeed
import graymatter.sec.common.cli.SelfValidatingCommand
import graymatter.sec.common.crypto.BinaryEncoding
import graymatter.sec.common.encodeBinary
import graymatter.sec.common.validation.Validator
import graymatter.sec.usecase.GenerateRandomBytesUseCase
import picocli.CommandLine.*

@Command(name = "generate-bytes", description = ["Generate random bytes"])
class GenerateRandomBytes : SelfValidatingCommand() {


    @ArgGroup
    val output = OutputTargetProvider()
/*

    @Mixin
    val seed = GivenSeed()
*/

    @set:Option(names = ["--prefix"])
    var prefix: String? = null

    @set:Option(names = ["-b", "--bytes"], required = true)
    var numBytes: Int? = null

    @set:Option(names = ["-e", "--enc"], required = true, defaultValue = "base64")
    var encoding: BinaryEncoding = BinaryEncoding.Base64

    @set:Option(names = ["-n", "--repeat"], defaultValue = "1")
    var repeatGenerateRandom: Int = 1

    override fun Validator.validateSelf() {
    }

    override fun performAction() {
        println(this)
        with(GenerateRandomBytesUseCase(null)) {
            repeat(repeatGenerateRandom) {
                println(buildString {
                    prefix?.let(this::append)
                    append(generate(numBytes!!).encodeBinary(encoding))
                })
            }
        }
    }

    override fun applyDefaults() {
        output.takeUnless { it.isAvailable }?.setOutputToStdOut()
    }

}
