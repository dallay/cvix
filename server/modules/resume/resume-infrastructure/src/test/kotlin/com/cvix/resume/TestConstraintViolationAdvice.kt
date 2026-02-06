package com.cvix.resume

import com.cvix.controllers.ERROR_CATEGORY
import com.cvix.controllers.ERROR_PAGE
import com.cvix.controllers.MESSAGE_KEY
import com.cvix.controllers.TIMESTAMP
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.ElementKind
import jakarta.validation.Path
import java.lang.reflect.Method
import java.net.URI
import java.time.Instant
import kotlin.reflect.jvm.kotlinFunction
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class TestConstraintViolationAdvice {

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ProblemDetail {
        val pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST)
        pd.title = "validation failed"
        pd.detail = "Request parameter validation failed. Please check the provided values."
        pd.type = URI.create("$ERROR_PAGE/validation/constraint-violation")
        pd.setProperty(ERROR_CATEGORY, "VALIDATION")
        pd.setProperty(MESSAGE_KEY, "error.validation.failed")
        val errors = ex.constraintViolations.map { violation ->
            mapOf(
                "field" to resolveField(violation),
                "message" to (violation.message ?: "Invalid value"),
                "rejectedValue" to violation.invalidValue,
            )
        }
        pd.setProperty("errors", errors)
        pd.setProperty(TIMESTAMP, Instant.now().toString())
        return pd
    }

    private fun resolveField(violation: ConstraintViolation<*>): String {
        val path = violation.propertyPath
        val methodName = path.methodName()
        val parameterIndex = path.parameterIndex()

        if (methodName == null || parameterIndex == null) {
            return path.toString()
        }

        val method = findMethod(violation.rootBeanClass, methodName, violation)
            ?: return path.toString()

        val parameterName = method.kotlinFunction
            ?.parameters
            ?.filter { it.kind == kotlin.reflect.KParameter.Kind.VALUE }
            ?.getOrNull(parameterIndex)
            ?.name
            ?: method.parameters.getOrNull(parameterIndex)?.name
            ?: return path.toString()

        return "$methodName.$parameterName"
    }

    private fun findMethod(
        rootClass: Class<*>,
        methodName: String,
        violation: ConstraintViolation<*>
    ): Method? {
        val executableParams = violation.executableParameters?.size

        return rootClass.methods.firstOrNull { method ->
            method.name == methodName &&
                (executableParams == null || method.parameterCount == executableParams)
        } ?: rootClass.methods.firstOrNull { method ->
            method.name == methodName && method.parameterCount > 0
        }
    }

    private fun Path.methodName(): String? =
        this.firstOrNull { node -> node.kind == ElementKind.METHOD }?.name

    private fun Path.parameterIndex(): Int? =
        this.firstOrNull { node -> node.kind == ElementKind.PARAMETER }
            ?.let { node -> (node as? Path.ParameterNode)?.parameterIndex }
}
