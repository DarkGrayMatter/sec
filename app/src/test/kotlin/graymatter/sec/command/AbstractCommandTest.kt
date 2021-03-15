package graymatter.sec.command

import graymatter.sec.App
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.io.TempDir
import picocli.CommandLine
import picocli.CommandLine.populateCommand
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractCommandTest<T : Runnable> {

    protected lateinit var givenWorkingDir: File
        private set

    protected lateinit var givenCommand: T
        private set

    protected lateinit var givenCommandLine: CommandLine
        private set

    protected val hasGivenCommandLine: Boolean
        get() = this::givenCommandLine.isInitialized

    @BeforeAll
    fun setUpWorkingDir(@TempDir dir: File) {
        givenWorkingDir = dir
    }

    @BeforeEach
    open fun setUp() {
        this.givenCommand = setupCommand()
        this.givenCommandLine = App.createCommandLine(givenCommand)
    }

    protected abstract fun setupCommand(): T

    protected fun givenCommandLineOf(vararg args: String) {
        println("GIVEN-COMMAND-LINE:  ${givenCommand.javaClass.simpleName} - ${
            buildString {
                append("[")
                args.joinTo(this, separator = " ")
                append("]")
            }
        }")
        this.givenCommandLine = populateCommand(givenCommandLine, * args)
    }

    protected fun whenRunningCommand() {
        givenCommand.run()
    }

    protected fun file(name: String): File = File(givenWorkingDir, name)
}
