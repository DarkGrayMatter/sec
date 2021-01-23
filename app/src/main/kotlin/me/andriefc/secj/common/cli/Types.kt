package me.andriefc.secj.common.cli

sealed class CommandLogLevel(private val priority: Int) : Comparable<CommandLogLevel> {

    object SILENT : CommandLogLevel(0)
    object INFO : CommandLogLevel(1)
    object ERROR : CommandLogLevel(2)
    object DEBUG : CommandLogLevel(3)

    override fun compareTo(other: CommandLogLevel): Int = priority.compareTo(other.priority)
}
