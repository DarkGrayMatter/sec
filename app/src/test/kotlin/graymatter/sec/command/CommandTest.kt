package graymatter.sec.command

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut
import graymatter.sec.App
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class CommandTest<T : Runnable> {

    private enum class CliBuildState {
        NotGiven,
        Building,
        CompletelyAvailable,
        PartiallyAvailable
    }

    private var cliState = CliBuildState.NotGiven

    protected lateinit var givenWorkingDir: File
        private set

    private lateinit var givenCommand: T
    private lateinit var givenCommandLine: CommandLine

    protected val hasGivenCommandLine: Boolean
        get() = this::givenCommandLine.isInitialized

    protected var commandOutputLines: List<String>? = null
        private set

    protected var commandOutputRaw: String? = null
        private set

    private val givenCliArgs = mutableListOf<String>()

    private var callCounter = 0


    @BeforeAll
    fun setUpWorkingDir(@TempDir dir: File) {
        givenWorkingDir = dir
    }

    @BeforeEach
    open fun setUp() {
        this.givenCliArgs.clear()
        this.givenCommand = newCommand()
        this.callCounter = 0
    }

    protected abstract fun newCommand(): T

    protected fun cliArgs(vararg args: String) {
        args.forEach(givenCliArgs::add)
        cliState = CliBuildState.Building
    }

    protected open fun buildCommandLine() = Unit

    private fun buildCliArgs(): Array<String> {

        cliState = when (cliState) {
            CliBuildState.PartiallyAvailable, CliBuildState.Building -> {
                CliBuildState.PartiallyAvailable
            }
            CliBuildState.NotGiven -> {
                buildCommandLine()
                CliBuildState.CompletelyAvailable
            }
            CliBuildState.CompletelyAvailable -> {
                buildCommandLine()
                CliBuildState.CompletelyAvailable
            }
        }

        return givenCliArgs.toTypedArray()
    }

    protected open fun whenRunningCommand() {

        commandOutputLines = null
        commandOutputRaw = null

        if (callCounter > 0) {
            givenCommand = newCommand()
        }

        val args = buildCliArgs()

        this.givenCommandLine = App.createCommandLine(givenCommand)
        this.givenCommandLine.parseArgs(* args)

        println("""
            +--------------------------------------------------->
            | sec ${givenCommandLine.commandSpec.name()} ${buildString { args.joinTo(this, " ") }}
            +--------------------------------------------------->
            """.trimIndent())

        commandOutputRaw = tapSystemOut { givenCommand.run() }?.also { print(it) }
        commandOutputLines = commandOutputRaw?.lines()?.dropLastWhile { eof -> eof.isBlank() || eof.isEmpty() }

        ++callCounter
        givenCliArgs.clear()
    }

    protected fun file(name: String): File = File(givenWorkingDir, name)
}
