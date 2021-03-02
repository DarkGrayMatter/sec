package graymatter.sec.common.validation

interface ValidationErrors : List<String> {
    operator fun get(validation: Validator.Validation): String?
    operator fun contains(validation: Validator.Validation): Boolean
}
