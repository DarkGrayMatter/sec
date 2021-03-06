package graymatter.sec.common.crypto

import com.palantir.config.crypto.KeyWithType
import java.io.File


fun KeyWithType(file: File): KeyWithType = KeyWithType.fromString(file.readText())
