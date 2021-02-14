@file:JvmName("Closeables")
package graymatter.sec.common.io

import java.io.Closeable

fun <R> R.tryClose(): Boolean {
    return when (val closable = this) {
        is AutoCloseable -> {
            closable.close()
            true
        }
        is Closeable -> {
            closable.close()
            true
        }
        else -> {
            false
        }
    }
}
