package graymatter.sec.command

import graymatter.sec.App
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractCommandTest<T : Runnable> {

    protected lateinit var givenWorkingDir: File
        private set

    protected lateinit var givenCommand: T
        private set

    private lateinit var givenCommandLine: CommandLine

    protected val hasGivenCommandLine: Boolean
        get() = this::givenCommandLine.isInitialized

    private val givenCliArgs = mutableListOf<String>()

    @BeforeAll
    fun setUpWorkingDir(@TempDir dir: File) {
        givenWorkingDir = dir
    }

    @BeforeEach
    open fun setUp() {
        this.givenCliArgs.clear()
        this.givenCommand = setupCommand()
    }

    protected abstract fun setupCommand(): T

    protected fun cliArgs(vararg  args: String) {
        args.forEach(givenCliArgs::add)
    }

    protected open fun whenRunningCommand() {
        val args = givenCliArgs.toTypedArray()
        this.givenCommandLine = App.createCommandLine(givenCommand)
        this.givenCommandLine.parseArgs(* args)
        println("""
            +--------------------------------------------------->
            | sec ${givenCommandLine.commandSpec.name()} ${buildString { args.joinTo(this, " ") }}
            +--------------------------------------------------->
            """.trimIndent())
        givenCommand.run()
    }

    protected fun file(name: String): File = File(givenWorkingDir, name)
}
