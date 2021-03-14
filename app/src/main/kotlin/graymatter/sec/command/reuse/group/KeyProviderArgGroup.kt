package graymatter.sec.command.reuse.group

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.App
import graymatter.sec.common.exception.failCommand
import graymatter.sec.common.file
import graymatter.sec.common.resourceFile
import picocli.CommandLine.Option
import java.io.File

/**
 * This class represents the various encryption/decryption keys can be passed
 * to yhe command line.
 *
 * As usual we cater for the following key sources:
 *
 * - A value on the command line.
 * - A file on the  host running the application.
 * - A file available as resource on the classpath.
 * - An environment variable.
 *
 * > **NOTE:** Only one kind of supplier can be used.
 */
class KeyProviderArgGroup {

    val isSupplied: Boolean get() = keySupplier != null

    var keyUri: String? = null
        private set

    private var keySupplier: (() -> KeyWithType)? = null

    @Option(
        names = ["--key","-k"],
        description = ["Retrieves key from a file."],
        required = true
    )
    fun setKeyFile(file: File) {
        keySupplier = { KeyWithType.fromString(file.readText()) }
        keyUri = file.toURI().toString()
    }

    @Option(
        names = ["--key-res"],
        required = true,
        description = ["Retrieve key from file on the application classpath."]
    )
    fun setKeyFileFromClassPath(resource: String) {
        keyUri = Any::class.java.getResource(resource)?.toURI()?.toString()
        keySupplier = { resourceFile<App>(resource).readText().let(KeyWithType::fromString) }
    }

    @Option(
        names = ["--key-val"],
        required = true,
        description = ["Retrieves key from directly from the command line."]
    )
    fun setKeyValue(string: String) {
        keySupplier = { KeyWithType.fromString(string) }
    }

    @Option(
        names = ["--key-env"],
        description = ["Retrieves key from an environment variable."],
        required = true
    )
    fun setKeyFromEnvVariable(variable: String) {
        keySupplier = {
            val value = System.getenv(variable) ?: failCommand("Key environment variable \"$variable\" is undefined.")
            KeyWithType.fromString(value)
        }
    }

    val keyWithType: KeyWithType? get() = keySupplier?.invoke()
}
