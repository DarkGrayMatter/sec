package graymatter.sec.command.reuse.mixin

import graymatter.sec.common.crypto.BinaryEncoding
import picocli.CommandLine.Option

/**
 * A CLI requirement for seed value. Supports standard binary encoding of Base16, Hex(lower case Base16), as well
 * as base 32 and base 64.
 */
class GivenSeed() {

    @JvmOverloads
    constructor(encodedValue: String, binaryEncoding: BinaryEncoding = BinaryEncoding.Base64) : this() {
        this.binaryEncoding = binaryEncoding
        this.encodedValue = encodedValue
    }

    @Option(
        names = ["--seed"],
        required = true,
        description = [
            "Binary seed value (encoded as text)."
        ],
    )
    var encodedValue: String? = null
        private set

    @Option(
        names = ["--seed-enc","--seed-encoding"],
        defaultValue = "64",
        description = [
            "Binary encoding scheme used to represent the seed. The following encoding schemes are available:",
            "   - Hexadecimal (lower case) use: hex",
            "   - Hexadecimal (upper case) use: 16, base16",
            "   - Base 32 use: 32, base32",
            "   - Base 63 use: 64, base64"
        ],
    )
    var binaryEncoding: BinaryEncoding = BinaryEncoding.Base64
        private set

    fun asBytes(): ByteArray? = encodedValue?.let(binaryEncoding::decode)
}
