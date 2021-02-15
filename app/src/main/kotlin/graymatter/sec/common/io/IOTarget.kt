@file:Suppress("MemberVisibilityCanBePrivate")

package graymatter.sec.common.io

import java.io.Closeable
import java.io.EOFException

/**
 * > **NB:** This really just a _crutch_ because Kotlin does not make it possible to delegate to class instances.
 *
 * To access the underlying IO target, supply a lambda to the [invoke] function.
 *
 * @param getTarget A function which provides access to the target resource.
 * @param closing A function which will be called to close the underlying IO resource via the [tryClose] extension function.
 *
 * @see [tryClose]
 */
class IOTarget<R>(
    getTarget: () -> R,
    private val closing: ((R) -> Unit) = { it.tryClose() }
) : Closeable {

    var eof: Boolean = false
        private set

    private val target: () -> R = {
        when {
            eof -> throw EOFException()
            else -> getTarget()
        }
    }

    @Synchronized
    override fun close() {
        eof = try {
            target().also(closing)
            true
        } catch (e: EOFException) {
            if (eof) throw e
            true
        }
    }

    @Synchronized
    operator fun <T> invoke(performIO: R.() -> T): T {
        return try {
            target().performIO()
        } catch (e: EOFException) {
            eof = true
            throw e
        }
    }
}
