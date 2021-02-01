package becode.sec.common


fun String.tr(): String {
    return when {
        isEmpty() -> this
        else -> lineSequence()
            .map { it.trim() }
            .filterNot { it.isEmpty() }
            .let { parts -> buildString { parts.joinTo(this, separator = " ") } }
    }
}

inline fun requireState(inState: Boolean, errorMessage: () -> String) {
    if (!inState) {
        throw IllegalStateException(errorMessage())
    }
}

inline fun requireState(state: String, inState: () -> Boolean) {
    if (!inState()) {
        throw IllegalStateException("Expected state: $state")
    }
}

enum class Separator(val char: Char) {
    Comma(','),
    Dot('.'),
    Slash('/'),
    DosSlash('\\'),
    Pipe('|'),
    Tab('\t'),
    SemiColon(';')
}
