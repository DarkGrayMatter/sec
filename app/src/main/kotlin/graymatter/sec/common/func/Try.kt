package graymatter.sec.common.func


interface Try<T> {

    fun <E> onException(exceptionClass: Class<out E>, handleException: (E) -> Unit): Try<T>
            where E : Exception

    fun onSuccess(action: (T) -> Unit)

    fun <R> let(fromResult: (T) -> R): R

    fun fold(mapException: (Exception) -> T): T

    fun <R> thenLet(map: (T) -> R): Try<R>

}

inline fun <reified E : Exception,T> Try<T>.onException(noinline handleException: (E) -> Unit): Try<T> {
    return onException(E::class.java, handleException)
}

@Suppress("FunctionName")
fun <T> Try(action: Try<T>.() -> T): Try<T> {
    return object : Try<T> {

        private val r = runCatching(action).onFailure { throwable ->
            throwable.takeUnless { it !is Exception }?.also { throw it }
        }

        override fun <E : Exception> onException(exceptionClass: Class<out E>, handleException: (E) -> Unit): Try<T> {
            return apply {
                r.exceptionOrNull()
                    ?.takeIf { exceptionClass.isInstance(this) }
                    ?.also { handleException(exceptionClass.cast(it)) }
            }
        }

        override fun fold(mapException: (Exception) -> T): T {
            return r.getOrElse { mapException(it as Exception) }
        }

        override fun <R> thenLet(map: (T) -> R): Try<R> {
            return Try { map(r.getOrThrow()) }
        }

        override fun onSuccess(action: (T) -> Unit) {
            r.onSuccess(action)
        }

        override fun <R> let(fromResult: (T) -> R): R {
            return r.getOrThrow().let(fromResult)
        }
    }
}


