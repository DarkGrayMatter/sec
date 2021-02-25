package graymatter.sec.common.validation

import graymatter.sec.common.exception.failCommand

fun ValidationTarget.validate(errorHandler: ValidationErrorHandler) {

    val errorBundle = mutableListOf<String>().run {
        performValidation(this::add)
        toList().takeUnless { it.isEmpty() }
    }

    errorBundle?.also(errorHandler::fail)
}

fun ValidationTarget.validated() {
    validate { errorBundle ->
        failCommand(
            errorBundle.joinToString(
                prefix = "",
                separator = "\n"
            )
        )
    }
}
