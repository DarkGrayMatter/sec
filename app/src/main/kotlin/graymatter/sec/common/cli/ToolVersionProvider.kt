package graymatter.sec.common.cli

import graymatter.sec.common.Properties
import picocli.CommandLine

object ToolVersionProvider : CommandLine.IVersionProvider {

    private val resource = javaClass.getResource("/graymatter/sec/version.properties")
    private val info by lazy {
        resource?.openStream()
            ?.use { Properties(it) }
            ?.run {
                arrayOf(
                    "version: ${getProperty("version")}",
                    "build.timestamp: ${getProperty("build.ts")}"
                )
            }
            ?: emptyArray()

    }

    override fun getVersion(): Array<String> = info
}
