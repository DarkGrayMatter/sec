package graymatter.sec.common.validation

interface ValidationErrors : List<String> {
    operator fun get(validation: ValidationContext.Validation): String?
    operator fun contains(validation: ValidationContext.Validation): Boolean
}
