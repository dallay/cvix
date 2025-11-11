package com.loomify.resume.infrastructure.validation

import com.loomify.resume.infrastructure.web.request.GenerateResumeRequest
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Custom validation annotation to ensure resume has at least one content section.
 * Per FR-001: Resume must have at least one of work experience, education, or skills.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ResumeContentValidator::class])
@MustBeDocumented
annotation class ValidResumeContent(
    val message: String = "Resume must have at least one of: work experience, education, or skills",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

/**
 * Validator implementation for ValidResumeContent annotation.
 * Validates that a GenerateResumeRequest contains at least one content section.
 */
class ResumeContentValidator : ConstraintValidator<ValidResumeContent, GenerateResumeRequest> {

    override fun isValid(request: GenerateResumeRequest?, context: ConstraintValidatorContext?): Boolean {
        if (request == null) {
            return true // Let @NotNull handle null validation
        }

        val hasWorkExperience = !request.workExperience.isNullOrEmpty()
        val hasEducation = !request.education.isNullOrEmpty()
        val hasSkills = !request.skills.isNullOrEmpty()

        return hasWorkExperience || hasEducation || hasSkills
    }
}
