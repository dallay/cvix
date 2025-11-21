package com.loomify.resume.application

import com.loomify.common.domain.vo.email.Email
import com.loomify.resume.application.create.CreateResumeCommand
import com.loomify.resume.application.delete.DeleteResumeCommand
import com.loomify.resume.application.get.GetResumeQuery
import com.loomify.resume.application.list.ListResumesQuery
import com.loomify.resume.application.update.UpdateResumeCommand
import com.loomify.resume.domain.Basics
import com.loomify.resume.domain.CompanyName
import com.loomify.resume.domain.FullName
import com.loomify.resume.domain.JobTitle
import com.loomify.resume.domain.PhoneNumber
import com.loomify.resume.domain.Resume
import com.loomify.resume.domain.ResumeDocument
import com.loomify.resume.domain.ResumeDocumentId
import com.loomify.resume.domain.WorkExperience
import java.time.Instant
import java.util.UUID

/**
 * Test fixtures for resume CRUD tests.
 * Provides factory functions for creating test data.
 */
object ResumeTestFixtures {

    fun createValidResume(
        name: String = "John Doe",
        email: String = "john.doe@example.com",
        phone: String = "+1234567890"
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
        updatedBy: String = "test@example.com"
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
        createdBy: String = "test@example.com"
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
        expectedUpdatedAt: String? = null
    ): UpdateResumeCommand = UpdateResumeCommand(
        id = id,
        userId = userId,
        workspaceId = workspaceId,
        title = title,
        content = content,
        updatedBy = updatedBy,
        expectedUpdatedAt = expectedUpdatedAt,
    )

    fun createDeleteCommand(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID()
    ): DeleteResumeCommand = DeleteResumeCommand(
        id = id,
        userId = userId,
    )

    fun createGetQuery(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID()
    ): GetResumeQuery = GetResumeQuery(
        id = id,
        userId = userId,
    )

    fun createListQuery(
        userId: UUID = UUID.randomUUID(),
        workspaceId: UUID = UUID.randomUUID(),
        limit: Int = 50,
        cursor: UUID? = null
    ): ListResumesQuery = ListResumesQuery(
        userId = userId,
        workspaceId = workspaceId,
        limit = limit,
        cursor = cursor,
    )
}
