package graymatter.sec.command

import graymatter.sec.command.reuse.group.InputSourceArgGroup
import graymatter.sec.command.reuse.group.KeyProviderArgGroup
import graymatter.sec.command.reuse.group.OutputTargetArgGroup
import graymatter.sec.command.reuse.mixin.InputFormatMixin
import graymatter.sec.command.reuse.mixin.OutputFormatMixin
import graymatter.sec.common.cli.verify
import picocli.CommandLine.*

@Command(
    name = "decrypt-config",
    description = ["Decrypts configuration document given an appropriate key."]
)
class DecryptConfig : Runnable {

    @Spec
    lateinit var spec: Model.CommandSpec

    @ArgGroup(
        exclusive = true,
        order = 0,
        heading = "Provide a source document to decrypt using one of the following arguments.%n"
    )
    lateinit var source: InputSourceArgGroup

    @ArgGroup(
        exclusive = true,
        order = 1,
        heading = "Provide a destination of the decrypted document using of the following arguments%n"
    )
    lateinit var destination: OutputTargetArgGroup

    @ArgGroup(
        exclusive = true,
        order = 2,
        heading = "Use any of the following arguments or options to specify a key for decryption:%n"
    )
    lateinit var keyProvider: KeyProviderArgGroup

    @Mixin
    val inputFormatMixin = InputFormatMixin()

    @Mixin
    val outputFormatMixin = OutputFormatMixin()

    override fun run() {
        verify()
    }

    private fun verify() {
        spec.verify {

            requires(this@DecryptConfig::source.isInitialized) {
                "No source document to decrypt was supplied."
            }

            requires(this@DecryptConfig::destination.isInitialized) {
                "No destination provided to output the decrypted document to."
            }

            val keyProviderValidation = requires(this@DecryptConfig::keyProvider.isInitialized) {
                "No decryption key supplied."
            }

            if (passed(keyProviderValidation)) {
                val r = keyProvider.runCatching { keyWithType }
                val cause = r.exceptionOrNull()
                requires(cause == null && r.isSuccess && r.getOrNull() != null) {
                    buildString {
                        append("Error loading encryption key from ${keyProvider.keyUri}.")
                        if (cause != null) {
                            append(" This was caused by a ${cause.javaClass.simpleName} error.")
                            if (cause.message?.isNotBlank() == false) {
                                append(" (${cause.message})")
                            }
                        }
                    }
                }
            }
        }
    }


}
