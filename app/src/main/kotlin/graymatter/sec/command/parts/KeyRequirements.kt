package graymatter.sec.command.parts

import picocli.CommandLine
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class KeyRequirements {

    @CommandLine.ArgGroup(exclusive = true, heading = "Key used during encryption (use any one of these:)%n")
    lateinit var provider: InputProvider
        internal set

    class InputProvider {

        private lateinit var openKeyFile: () -> InputStream

        @CommandLine.Option(names = ["--key-file"], description = ["File containing encryption key"])
        fun setKeyFile(keyFile: File) {
            openKeyFile = keyFile::inputStream
        }

        @CommandLine.Option(names = ["-k", "--key"], description = ["Key value on command line."])
        fun setKeyFromCommandLine(keyFromCommandLine: String) {
            openKeyFile = { keyFromCommandLine.byteInputStream() }
        }

        @CommandLine.Option(names = ["--key-resource"], description = ["Key file as a resource on the classpath."])
        fun setKeyFromClassPath(keyFromClassPath: String) {
            openKeyFile = {
                javaClass.getResourceAsStream(keyFromClassPath)
                    ?: throw FileNotFoundException(
                        "Could not find key file on classpath: $keyFromClassPath"
                    )
            }
        }

        fun readKeyFile(): InputStream = openKeyFile()
    }

}
