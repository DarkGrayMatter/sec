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

    private lateinit var encryptionKeyFile: File
    private lateinit var configFormat: ConfigFormat
    private lateinit var configFile: File
    private lateinit var encryptedFileQualifier: String
    private lateinit var selection: TargetPropertySelection

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
        description = ["The public key file to use for encrypting the configuration file"]
    )
    fun setKey(keyFile: File) {
        this.encryptionKeyFile = keyFile
    }

    @Option(
        names = ["-v"],
        required = true,
        description = ["File to encrypt."]
    )
    fun setConfigFile(file: File) {
        this.configFile = file
    }


    @ArgGroup(exclusive = true, multiplicity = "1")
    fun setPropertySelection(pvs: TargetPropertySelection) {
        selection = pvs
    }

    enum class ConfigFormat(accepts: FileFilter) : FileFilter by accepts {

        AUTO(FileFilter { false })
        ;

        companion object {
            fun select(file: File): ConfigFormat? = autoSelectables.firstOrNull { it.accept(file) }
            private val autoSelectables = values().toSet() - AUTO
        }
    }

    /**
     * This class specified which properties are selected for encryption
     */
    class TargetPropertySelection {

        private var containsPath = { _: String -> false }

        @Option(names = ["--all-paths"], required = false, description = ["Encrypts all possible data paths."])
        fun setAll(all: Boolean) {
            containsPath = { all }
        }

        @Option(names = ["--path-file"], required = false, description = ["File contain one path per line to encrypt."])
        fun setPathFile(file: File) {
            val filedKeys by lazy { file.readLines().map(String::trim).toSet() }
            containsPath = filedKeys::contains
        }

        @Option(names = ["--path", "-P"], required = false, description = ["Configuration paths to encrypt."])
        fun setPaths(paths: Set<String>) {
            containsPath = paths::contains
        }

        operator fun contains(path: String) = containsPath(path)
    }

    companion object {
        private const val DEFAULT_ENCRYPTED_QUALIFIER = "encrypted"
    }
}
