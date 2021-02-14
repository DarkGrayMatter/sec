@file:JvmName("Closeables")
package graymatter.sec.common.io

import java.io.Closeable

fun <R> R.tryClose(): Boolean {
    val closeable = this as? Closeable
    closeable?.close()
    return closeable != null
}
