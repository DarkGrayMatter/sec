package graymatter.sec.common.func


interface Tried<T> {

    fun <E> onException(exceptionClass: Class<out E>, handleException: (E) -> Unit): Tried<T>
            where E : Exception

    fun onSuccess(action: (T) -> Unit)

    fun <R> let(fromResult: (T) -> R): R

    fun fold(mapException: (Exception) -> T): T

    fun <R> thenLet(map: (T) -> R): Tried<R>

}

inline fun <reified E : Exception,T> Tried<T>.onException(noinline handleException: (E) -> Unit): Tried<T> {
    return onException(E::class.java, handleException)
}

@Suppress("FunctionName")
fun <T> Try(action: Tried<T>.() -> T): Tried<T> {
    return object : Tried<T> {

        private val r = runCatching(action).onFailure { throwable ->
            throwable.takeUnless { it !is Exception }?.also { throw it }
        }

        override fun <E : Exception> onException(exceptionClass: Class<out E>, handleException: (E) -> Unit): Tried<T> {
            return apply {
                r.exceptionOrNull()
                    ?.takeIf { exceptionClass.isInstance(this) }
                    ?.also { handleException(exceptionClass.cast(it)) }
            }
        }

        override fun fold(mapException: (Exception) -> T): T {
            return r.getOrElse { mapException(it as Exception) }
        }

        override fun <R> thenLet(map: (T) -> R): Tried<R> {
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


