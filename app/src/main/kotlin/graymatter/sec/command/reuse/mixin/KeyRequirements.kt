@file:Suppress("unused")

package graymatter.sec.command.reuse.mixin

import com.palantir.config.crypto.KeyWithType
import graymatter.sec.common.crypto.readKeyWithType
import graymatter.sec.common.resourceAt
import picocli.CommandLine
import java.io.File
import java.io.InputStream

class KeyRequirements {

    @CommandLine.ArgGroup(exclusive = true, heading = "Key used during encryption (use any one of these:)%n")
    lateinit var provider: InputProvider

    fun keyWithType(): KeyWithType {
        return provider.readKey().use(InputStream::readKeyWithType)
    }

    class InputProvider {

        private lateinit var openKeyFile: () -> InputStream

        @CommandLine.Option(names = ["-k", "--key"], description = ["File containing encryption key"])
        fun setKeyFile(keyFile: File) {
            openKeyFile = keyFile::inputStream
        }

        @CommandLine.Option(names = ["--key-text"], description = ["Key value on command line."])
        fun setKeyFromCommandLine(keyFromCommandLine: String) {
            openKeyFile = { keyFromCommandLine.byteInputStream() }
        }

        @CommandLine.Option(names = ["--key-res"], description = ["Key file as a resource on the classpath."])
        fun setKeyFromClassPath(keyFromClassPath: String) {
            openKeyFile = { resourceAt<KeyRequirements>(keyFromClassPath).openStream() }
        }

        fun readKey(): InputStream = openKeyFile()
    }

}
