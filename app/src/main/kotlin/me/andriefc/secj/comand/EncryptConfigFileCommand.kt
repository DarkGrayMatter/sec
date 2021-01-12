package me.andriefc.secj.comand

import me.andriefc.secj.comand.EncryptConfigFileCommand.ConfigFormat
import picocli.CommandLine.*
import java.io.File
import java.io.FileFilter

/**
 * This command encrypts a complete configuration file based on and a supplied [ConfigFormat]
 */
@Suppress("unused")
@Command(
    name = "encrypt-config",
    description = ["Encrypt configuration file. The following formats are supported: auto, json, yaml"]
)
class EncryptConfigFileCommand : Runnable {

    private lateinit var key: String
    private lateinit var configFormat: ConfigFormat
    private lateinit var configFile: File
    private lateinit var encryptedFileQualifier: String

    @Mixin
    lateinit var valueSelection: EncryptionValueSelectionOptions

    override fun run() {
        TODO("Not yet implemented")
    }

    @Option(
        names = ["--format"],
        description = ["The type of configuration formats supported."],
        required = true,
        defaultValue = "auto"
    )
    fun setConfigFormat(format: ConfigFormat) {
        this.configFormat = format
    }

    @Option(
        names = ["-k", "--key"],
        required = true,
        description = ["The public key to use for encrypting the configuration file"]
    )
    fun setKey(key: String) {
        this.key = key
    }

    @Option(
        names = ["-v"],
        required = true,
        description = ["File to encrypt."]
    )
    fun setConfigFile(file: File) {
        this.configFile = file
    }

    enum class ConfigFormat(accepts: FileFilter) : FileFilter by accepts {

        AUTO(FileFilter { false })
        ;

        companion object {

            fun select(file: File): ConfigFormat? = autoSelectables.firstOrNull { it.accept(file) }

            private val autoSelectables = values().toMutableSet().run {
                remove(AUTO)
                toSet()
            }
        }
    }

    /**
     * This class specified which keys are selected for encryption
     */
    class EncryptionValueSelectionOptions {

        @Option(names = ["--all-paths"], required = false, description = ["Encrypts all possible data paths."])
        fun setAll(all: Boolean) = Unit

        @Option(names = ["--path-file"], required = false, description = ["File contain one path per line to encrypt."])
        fun setPathFile(file: File) = Unit

        @Option(names = ["--paths"], required = false, description = ["Configuration paths to encrypt."])
        fun setPaths(paths: Set<String>) = Unit


    }

    companion object {
        private const val DEFAULT_ENCRYPTED_QUALIFIER = "encrypted"
    }
}
