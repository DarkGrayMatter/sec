package becode.sec.common


fun String.tr(): String {
    return when {
        isEmpty() -> this
        else -> trimIndent().lines().joinToString(" ") { it.trim() }
    }
}
