package graymatter.sec.common.io

import java.io.Closeable
import java.io.EOFException

class ReportWriter(
    private val reportOut: Action,
    private val closeNow: () -> Unit
) : Closeable {
    interface Action {
        fun <T : Any> report(actionLabel: String, reportDataClass: Class<T>, reportData: T?)
    }

    private var closed = false
    private var mutex = object {}

    fun <T : Any> report(action: String, dataClass: Class<T>, data: T?) {
        synchronized(mutex) {
            if (closed) {
                throw EOFException("Reporting has been closed.")
            }
            reportOut.report(action, dataClass, data)
        }
    }

    override fun close() {
        synchronized(mutex) {
            if (!closed) {
                closeNow()
                closed = true
            }
        }
    }
}

inline fun <reified T : Any> ReportWriter.report(actionLabel: String, data: T?) {
    report(actionLabel, T::class.java, data)
}

fun reportAction(out: (actionLabel: String, reportDataClass: Class<Any>, reportData: Any?) -> Unit): ReportWriter.Action {
    return object : ReportWriter.Action {
        override fun <T : Any> report(actionLabel: String, reportDataClass: Class<T>, reportData: T?) {
            @Suppress("UNCHECKED_CAST")
            out(actionLabel, reportDataClass as Class<Any>, reportData)
        }
    }
}

