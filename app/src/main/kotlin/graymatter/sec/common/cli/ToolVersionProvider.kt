package graymatter.sec.common.cli

import graymatter.sec.App
import graymatter.sec.common.Properties
import graymatter.sec.common.resourceAt
import picocli.CommandLine

object ToolVersionProvider : CommandLine.IVersionProvider {

    private val applicationVersion: String
    private val applicationBuildArch: String
    private val applicationBuildDate: String

    init {

        val res = resourceAt<App>("/app.properties")
        val props = Properties(res)

        fun requiredProperty(property: String) = props.getProperty(property)
            ?: throw NoSuchElementException("Expected property $property in $res, but non was found.")

        applicationVersion = requiredProperty("application.version")
        applicationBuildArch = requiredProperty("application.build.arch")
        applicationBuildDate = requiredProperty("application.build.date")
    }

    override fun getVersion(): Array<String> = arrayOf(
        "Sec.Version: $applicationVersion",
        "Sec.Build.Date:  $applicationBuildDate",
        "Sec.Build.Architecture: $applicationBuildArch"
    )

}
