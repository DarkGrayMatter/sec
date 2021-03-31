package graymatter.sec.common.func

sealed class Either<out L, out R> {
    data class Left<L>(val value: L) : Either<L, Nothing>()
    data class Right<R>(val value: R) : Either<Nothing, R>()
}

inline fun <L, R, T> Either<L, R>.fold(foldLeft: (L) -> T, foldRight: (R) -> T): T {
    return when (this) {
        is Either.Left -> foldLeft(value)
        is Either.Right -> foldRight(value)
    }
}

fun <L> L.asLeft(): Either.Left<L> = Either.Left(this)
fun <R> R.asRight(): Either.Right<R> = Either.Right(this)

val <R> Either<*, R>.right: R
    get() {
        return when (this) {
            is Either.Left -> {
                val cause = value as? Throwable
                throw IllegalStateException(buildString {
                    append("Operation did not complete normally")
                    cause?.message?.also { append(":$it") }
                    append('.')
                }).also { cause?.let(it::initCause) }
            }
            is Either.Right -> value
        }
    }

val <L> Either<L, *>.left: L?
    get() {
        return when (this) {
            is Either.Left -> value
            is Either.Right -> null
        }
    }

inline fun <reified E, T> eitherTry(action: () -> T): Either<E, T> where E : Throwable {
    return try {
        right(action())
    } catch (e: Throwable) {
        (e as? E)?.asLeft() ?: throw e
    }
}

fun <R> right(value: R) = Either.Right(value)
fun <L> left(value: L) = Either.Left(value)

inline fun <L, R, T> Either<L, R>.map(mapRight: (R) -> T): Either<L, T> {
    return when (this) {
        is Either.Left -> this
        is Either.Right -> right(mapRight(value))
    }
}

inline fun <L, R, T> Either<L, R>.mapLeft(mapLeft: (L) -> T): Either<T, R> {
    return when (this) {
        is Either.Left -> left(mapLeft(value))
        is Either.Right -> this
    }
}

inline fun <L, R, L1, R1> Either<L, R>.map(mapLeft: (L) -> L1, mapRight: (R) -> R1): Either<L1, R1> {
    return when (this) {
        is Either.Left -> left(mapLeft(value))
        is Either.Right -> right(mapRight(value))
    }
}

fun <L,R> Either<L,R>.onLeft(receivedLeft:(L) -> Unit): Either<L, R> = apply {
    if (this is Either.Left) {
        receivedLeft(value)
    }
}
fun <L,R> Either<L,R>.onRight(receivedRight:(R) -> Unit): Either<L, R> = apply {
    if (this is Either.Right) {
        receivedRight(value)
    }
}
