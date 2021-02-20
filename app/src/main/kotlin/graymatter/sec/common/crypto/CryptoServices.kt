package graymatter.sec.common.crypto

import com.palantir.config.crypto.Key
import com.palantir.config.crypto.KeyWithType
import com.palantir.config.crypto.algorithm.KeyType
import java.io.InputStream

/**
 * Reads key with type from an input stream.
 */
fun InputStream.readKeyWithType(): KeyWithType {
    return KeyWithType.fromString(String(readAllBytes()))
}


operator fun KeyWithType.component1(): Key = key
operator fun KeyWithType.component2(): KeyType = type
