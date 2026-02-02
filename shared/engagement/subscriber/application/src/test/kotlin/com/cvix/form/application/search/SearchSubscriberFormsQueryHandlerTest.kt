package com.cvix.form.application.search

import com.cvix.UnitTest
import com.cvix.common.domain.model.WorkspaceId
import com.cvix.common.domain.presentation.pagination.CursorPageResponse
import com.cvix.common.domain.security.WorkspaceAuthorization
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.domain.SubscriptionFormFinderRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach

@UnitTest
internal class SearchSubscriberFormsQueryHandlerTest {
    private val formFinderRepository: SubscriptionFormFinderRepository = mockk()
    private val searcher = SubscriberFormsSearcher(formFinderRepository)
    private val workspaceAuthorization: WorkspaceAuthorization = mockk(relaxUnitFun = true)
    private val queryHandler = SearchSubscriberFormsQueryHandler(workspaceAuthorization, searcher)

    private val workspaceUuid = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        coEvery { workspaceAuthorization.ensureAccess(workspaceUuid, userId) } returns Unit
    }

    @Test
    fun `should search forms`() = runTest {
        val form1 = SubscriberFormStub.randomForm(workspaceId = WorkspaceId(workspaceUuid))
        val form2 = SubscriberFormStub.randomForm(workspaceId = WorkspaceId(workspaceUuid))
        val pageResponse = CursorPageResponse(
            data = listOf(form1, form2),
            prevPageCursor = null,
            nextPageCursor = null,
        )

        coEvery { formFinderRepository.search(any(), any(), any(), any()) } returns pageResponse

        val query = SearchSubscriberFormsQuery(workspaceUuid, userId)
        val result = queryHandler.handle(query)

        assertEquals(2, result.data.size)
        assertEquals(form1.id.value.toString(), result.data.toList()[0].id)
        assertEquals(form2.id.value.toString(), result.data.toList()[1].id)
    }
}
