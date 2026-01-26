package com.cvix.form.application.details

import com.cvix.UnitTest
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.domain.SubscriptionFormFinderRepository
import com.cvix.form.domain.SubscriptionFormId
import com.cvix.form.domain.exception.SubscriptionFormNotFoundException
import io.mockk.coEvery
import io.mockk.mockk
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class DetailSubscriberFormQueryHandlerTest {
    private val formFinderRepository: SubscriptionFormFinderRepository = mockk()
    private val formFetcher = DetailSubscriberFormFetcher(formFinderRepository)
    private val workspaceAuthorization: WorkspaceAuthorization = mockk(relaxUnitFun = true)
    private val queryHandler = DetailSubscriberFormQueryHandler(workspaceAuthorization, formFetcher)

    private val workspaceUuid = UUID.randomUUID()
    private val userId = UUID.randomUUID()
    private val formId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        coEvery { workspaceAuthorization.ensureAccess(workspaceUuid, userId) } returns Unit
    }

    @Test
    fun `should get form details`() = runTest {
        val form = SubscriberFormStub.randomForm(
            id = SubscriptionFormId(formId),
            workspaceId = WorkspaceId(workspaceUuid),
        )
        coEvery {
            formFinderRepository.findByFormIdAndWorkspaceId(
                SubscriptionFormId(formId),
                WorkspaceId(workspaceUuid),
            )
        } returns form

        val query = DetailSubscriberFormQuery(formId, workspaceUuid, userId)
        val result = queryHandler.handle(query)

        assertEquals(formId.toString(), result.id)
        assertEquals(form.name, result.name)
    }

    @Test
    fun `should fail when form is not found`() = runTest {
        coEvery { formFinderRepository.findByFormIdAndWorkspaceId(any(), any()) } returns null

        val query = DetailSubscriberFormQuery(formId, workspaceUuid, userId)

        assertFailsWith<SubscriptionFormNotFoundException> {
            queryHandler.handle(query)
        }
    }
}
