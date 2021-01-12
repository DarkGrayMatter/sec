@file:JvmName("Closeables")
package me.andriefc.secj.core.io

import java.io.Closeable

fun <R> R.tryClose(): Boolean {
    val closeable = this as? Closeable
    closeable?.close()
    return closeable != null
}
