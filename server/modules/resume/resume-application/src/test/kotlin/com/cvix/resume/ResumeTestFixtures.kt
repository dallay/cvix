package com.cvix.resume

import com.cvix.common.domain.vo.email.Email
import com.cvix.resume.application.create.CreateResumeCommand
import com.cvix.resume.application.update.UpdateResumeCommand
import com.cvix.resume.domain.Basics
import com.cvix.resume.domain.CompanyName
import com.cvix.resume.domain.FullName
import com.cvix.resume.domain.JobTitle
import com.cvix.resume.domain.PhoneNumber
import com.cvix.resume.domain.Resume
import com.cvix.resume.domain.ResumeDocument
import com.cvix.resume.domain.ResumeDocumentId
import com.cvix.resume.domain.WorkExperience
import java.time.Instant
import java.util.UUID

/**
 * Test fixtures for resume application tests.
 */
object ResumeTestFixtures {

    fun createValidResume(
        name: String = "John Doe",
        email: String = "john.doe@example.com",
        phone: String = "+1234567890",
    ): Resume = Resume(
        basics = Basics(
            name = FullName(name),
            email = Email(email),
            phone = PhoneNumber(phone),
        ),
        work = listOf(
            WorkExperience(
                name = CompanyName("ACME Corp"),
                position = JobTitle("Software Engineer"),
                startDate = "2020-01-01",
            ),
        ),
    )

    fun createResumeDocument(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        title: String = "John Doe",
        content: Resume = createValidResume(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
        createdBy: String = "test@example.com",
        updatedBy: String = "test@example.com",
    ): ResumeDocument = ResumeDocument(
        id = ResumeDocumentId(id),
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedBy = updatedBy,
        updatedAt = updatedAt,
    )

    fun createCreateCommand(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        title: String? = null,
        content: Resume = createValidResume(),
        createdBy: String = "test@example.com",
    ): CreateResumeCommand = CreateResumeCommand(
        id = id,
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        createdBy = createdBy,
    )

    fun createUpdateCommand(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        title: String? = null,
        content: Resume = createValidResume(),
        updatedBy: String = "test@example.com",
        expectedUpdatedAt: Instant? = null,
    ): UpdateResumeCommand = UpdateResumeCommand(
        id = id,
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        updatedBy = updatedBy,
        expectedUpdatedAt = expectedUpdatedAt,
    )
}
