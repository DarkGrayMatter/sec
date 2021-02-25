package graymatter.sec.common.cli

interface CliValidationTarget {
    fun validate(failWith: (error: String) -> String)
}
