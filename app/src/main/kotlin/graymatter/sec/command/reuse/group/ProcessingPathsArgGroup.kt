package graymatter.sec.command.reuse.group

import graymatter.sec.App
import graymatter.sec.common.queueOf
import graymatter.sec.common.resourceAt
import picocli.CommandLine.Option
import java.io.File

/**
 * Allows the user to specified paths based on Ant style matchers used
 * to match the processing of configuration documents. The last specified
 * path wins when it comes to evaluation.
 *
 * Processing paths can be supplied by:
 *
 * - A single path on the command line (more than one is allowed)
 * - A file containing paths (one expression per line per file)
 * - Class path resource containing paths (one expression per line per resource)
 */
class ProcessingPathsArgGroup {

    private var paths: Path = Path.PathCollection()

    @Option(
        names = ["--path-val"],
        arity = "0..*",
        split = ";",
        paramLabel = "path",
        description = [
            "Ant style path expression to match"
        ],
    )
    fun setPaths(pathList: List<String>) {
        pathList.map { path -> Path.Value(path) }
            .forEach { path -> this.paths += path }
    }

    @Option(
        names = ["--path"],
        arity = "0..*",
        description = [
            "A file which list one path match expression per line."
        ]
    )
    fun setPathFile(file: File) {
        paths += Path.PathFile(file)
    }

    @Option(
        names = ["--path-res"],
        arity = "0..*",
        paramLabel = "pathFileOnClassPath",
        description = [
            "A file available on the class path. Contains one match expression per line."
        ]
    )
    fun setPathFromClassPath(resource: String) {
        paths += Path.PathClasspathResource(resource)
    }

    fun expandPaths(): List<String> {
        return mutableListOf<String>().run {
            val queue = queueOf(paths)
            while (queue.isNotEmpty()) {
                when (val p = queue.remove()) {
                    is Path.PathClasspathResource -> resourceAt<App>(p.resource).readText().lines().forEach(this::add)
                    is Path.PathCollection -> p.forEach(queue::add)
                    is Path.PathFile -> p.file.readLines().forEach(this::add)
                    is Path.Value -> add(p.value)
                }
            }
            toList()
        }
    }

    val isAvailable: Boolean
        get() = when (val p = paths) {
            is Path.PathClasspathResource -> true
            is Path.PathCollection -> p.isNotEmpty()
            is Path.PathFile -> true
            is Path.Value -> true
        }

    private sealed class Path {

        data class Value(val value: String) : Path()
        data class PathFile(val file: File) : Path()
        data class PathClasspathResource(val resource: String) : Path()

        @Suppress("CanSealedSubClassBeObject")
        class PathCollection() : MutableList<Path> by mutableListOf(), Path()
    }

    companion object {

        private operator fun Path.plus(other: Path): Path {
            return when (this) {
                is Path.PathCollection -> also { it.add(other) }
                else -> Path.PathCollection().also { it.add(other) }
            }
        }
    }

}



