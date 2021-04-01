package graymatter.sec.command.reuse.group

import graymatter.sec.common.crypto.BinaryEncoding
import picocli.CommandLine.Option

class SeedProvider {

    @Option(names = ["--seed"], description = ["Seed value encoded as a string value."])
    var seed: String? = null
        private set

    @Option(names = ["--seed-encoding"], description = ["Format of the seed value"], defaultValue = "hex")
    var seedEncoding: BinaryEncoding = BinaryEncoding.Hex
        private set

    fun seed(): ByteArray? = seed?.let(seedEncoding::decode)

}
