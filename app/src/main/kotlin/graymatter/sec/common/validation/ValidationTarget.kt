package graymatter.sec.common.validation

fun interface ValidationTarget {

    fun validate(validation: ValidationContext)

}
