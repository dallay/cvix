package com.cvix.resume.infrastructure.validation

import com.cvix.resume.infrastructure.http.request.GenerateResumeRequest
import com.cvix.resume.infrastructure.http.request.ResumeContentRequest
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
@Constraint(validatedBy = [ResumeContentValidator::class, ResumeContentRequestValidator::class])
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

    override fun isValid(
        request: GenerateResumeRequest?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (request == null) {
            return true // Let @NotNull handle null validation
        }

        val hasWorkExperience = !request.work.isNullOrEmpty()
        val hasEducation = !request.education.isNullOrEmpty()
        val hasSkills = !request.skills.isNullOrEmpty()

        return hasWorkExperience || hasEducation || hasSkills
    }
}

/**
 * Validator implementation for ValidResumeContent annotation for ResumeContentRequest.
 * Validates that a ResumeContentRequest contains at least one content section.
 */
class ResumeContentRequestValidator : ConstraintValidator<ValidResumeContent, ResumeContentRequest> {

    override fun isValid(
        request: ResumeContentRequest?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (request == null) {
            return true // Let @NotNull handle null validation
        }

        val hasWorkExperience = !request.work.isNullOrEmpty()
        val hasEducation = !request.education.isNullOrEmpty()
        val hasSkills = !request.skills.isNullOrEmpty()

        return hasWorkExperience || hasEducation || hasSkills
    }
}
