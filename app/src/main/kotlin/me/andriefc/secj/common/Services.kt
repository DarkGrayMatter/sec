package me.andriefc.secj.common


fun String.tr(): String {
    return when {
        isEmpty() -> this
        else -> trimIndent().lines().joinToString(" ") { it.trim() }
    }
}
