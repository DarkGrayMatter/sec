package becode.sec.common


fun String.tr(): String {
    return when {
        isEmpty() -> this
        else -> trimIndent().lines().joinToString(" ") { it.trim() }
    }
}

object PreCondition {
    inline fun require(condition: Boolean, message: () -> String) {
        if (!condition) {
            throw IllegalStateException(message())
        }
    }
}
