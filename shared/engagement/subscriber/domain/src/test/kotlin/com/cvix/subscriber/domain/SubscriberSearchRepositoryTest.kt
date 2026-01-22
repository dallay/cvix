package com.cvix.subscriber.domain

import com.cvix.UnitTest
import com.cvix.common.domain.model.Language
import com.cvix.common.domain.model.pagination.CursorPage
import com.cvix.common.domain.model.pagination.OffsetPage
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@UnitTest
internal class SubscriberSearchRepositoryTest {

    private val workspaceId = UUID.randomUUID()

    private fun sampleSubscriber(
        id: UUID = UUID.randomUUID(),
        email: String,
        source: String,
        status: SubscriberStatus = SubscriberStatus.PENDING,
        attributes: Attributes? = null,
    ): Subscriber = Subscriber.create(
        id = id,
        email = email,
        source = source,
        language = Language.ENGLISH,
        ipAddress = null,
        hasher = null,
        attributes = attributes,
        workspaceId = workspaceId,
    ).apply { updateStatus(status) }

    private class FakeSubscriberSearchRepository(private val store: MutableList<Subscriber>) :
        SubscriberSearchRepository {
        override suspend fun searchAllByOffset(
            criteria: com.cvix.common.domain.criteria.Criteria?,
            size: Int?,
            page: Int?,
            sort: com.cvix.common.domain.presentation.sort.Sort?,
        ): OffsetPage<Subscriber> {
            val perPage = size ?: store.size
            val p = page ?: 0
            val from = p * perPage
            val to = kotlin.math.min(from + perPage, store.size)
            val slice = if (from >= store.size) emptyList() else store.subList(from, to)
            val totalPages = if (perPage == 0) 0 else (store.size + perPage - 1) / perPage
            return OffsetPage(
                data = slice,
                total = store.size.toLong(),
                perPage = perPage,
                page = p,
                totalPages = totalPages,
            )
        }

        override suspend fun searchAllByCursor(
            criteria: com.cvix.common.domain.criteria.Criteria?,
            size: Int?,
            sort: com.cvix.common.domain.presentation.sort.Sort?,
            cursor: com.cvix.common.domain.presentation.pagination.Cursor?,
        ): CursorPage<Subscriber> {
            val n = size ?: store.size
            return CursorPage(data = store.take(n))
        }

        override suspend fun searchActive(): List<Subscriber> =
            store.filter { it.status == SubscriberStatus.ENABLED }

        override suspend fun findById(id: UUID): Subscriber? = store.find { it.id.value == id }

        override suspend fun findByEmailAndSource(email: String, source: String): Subscriber? =
            store.find { it.email.value == email && it.source.source == source }

        override suspend fun existsByEmailAndSource(email: String, source: String): Boolean =
            store.any { it.email.value == email && it.source.source == source }

        override suspend fun findAllByMetadata(key: String, value: String): List<Subscriber> =
            store.filter { it.attributes?.metadata?.get(key) == value }
    }

    @Test
    fun `existsByEmailAndSource returns true when subscriber exists`() = runBlocking {
        val s = sampleSubscriber(email = "a@x.com", source = "web")
        val repo = FakeSubscriberSearchRepository(mutableListOf(s))

        assertTrue(repo.existsByEmailAndSource("a@x.com", "web"))
    }

    @Test
    fun `existsByEmailAndSource returns false when subscriber does not exist`() = runBlocking {
        val repo = FakeSubscriberSearchRepository(mutableListOf())
        assertFalse(repo.existsByEmailAndSource("no@x.com", "web"))
    }

    @Test
    fun `findByEmailAndSource returns subscriber when found`() = runBlocking {
        val s = sampleSubscriber(email = "b@x.com", source = "api")
        val repo = FakeSubscriberSearchRepository(mutableListOf(s))

        val found = repo.findByEmailAndSource("b@x.com", "api")
        assertNotNull(found)
        assertEquals(s.email, found?.email)
    }

    @Test
    fun `findByEmailAndSource returns null when not found`() = runBlocking {
        val repo = FakeSubscriberSearchRepository(mutableListOf())
        assertNull(repo.findByEmailAndSource("missing@x.com", "api"))
    }

    @Test
    fun `findById returns subscriber when found`() = runBlocking {
        val id = UUID.randomUUID()
        val s = sampleSubscriber(id = id, email = "c@x.com", source = "mail")
        val repo = FakeSubscriberSearchRepository(mutableListOf(s))

        val found = repo.findById(id)
        assertNotNull(found)
        assertEquals(id, found?.id?.value)
    }

    @Test
    fun `findById returns null when not found`() = runBlocking {
        val repo = FakeSubscriberSearchRepository(mutableListOf())
        assertNull(repo.findById(UUID.randomUUID()))
    }

    @Test
    fun `findAllByMetadata returns subscribers matching metadata`() = runBlocking {
        val attrs = Attributes(tags = listOf("t"), metadata = mapOf("k" to "v"))
        val s = sampleSubscriber(email = "d@x.com", source = "web", attributes = attrs)
        val repo = FakeSubscriberSearchRepository(mutableListOf(s))

        val list = repo.findAllByMetadata("k", "v")
        assertEquals(1, list.size)
        assertEquals(s.email, list.first().email)
    }

    @Test
    fun `findAllByMetadata returns empty list when no match`() = runBlocking {
        val repo = FakeSubscriberSearchRepository(mutableListOf())
        val list = repo.findAllByMetadata("k", "v")
        assertTrue(list.isEmpty())
    }

    @Test
    fun `searchActive returns only enabled subscribers`() = runBlocking {
        val s1 =
            sampleSubscriber(email = "e1@x.com", source = "web", status = SubscriberStatus.ENABLED)
        val s2 =
            sampleSubscriber(email = "e2@x.com", source = "web", status = SubscriberStatus.DISABLED)
        val repo = FakeSubscriberSearchRepository(mutableListOf(s1, s2))

        val active = repo.searchActive()
        assertEquals(1, active.size)
        assertEquals(SubscriberStatus.ENABLED, active.first().status)
    }

    @Nested
    inner class SearchAllByOffset {
        @Test
        fun `returns paged subscribers for valid criteria`() = runBlocking {
            val items = (1..5).map { i -> sampleSubscriber(email = "$i@x.com", source = "web") }
            val repo = FakeSubscriberSearchRepository(items.toMutableList())

            val page = repo.searchAllByOffset(null, size = 2, page = 1, sort = null)
            assertEquals(2, page.data.size)
            assertEquals(5L, page.total)
            assertEquals(2, page.perPage)
            assertEquals(1, page.page)
        }

        @Test
        fun `returns empty page when no subscribers match`() = runBlocking {
            val repo = FakeSubscriberSearchRepository(mutableListOf())
            val page = repo.searchAllByOffset(null, size = 2, page = 0, sort = null)
            assertTrue(page.data.isEmpty())
            assertEquals(0L, page.total)
        }
    }

    @Nested
    inner class SearchAllByCursor {
        @Test
        fun `returns paged subscribers for valid cursor`() = runBlocking {
            val items = (1..4).map { i -> sampleSubscriber(email = "$i@x.com", source = "web") }
            val repo = FakeSubscriberSearchRepository(items.toMutableList())

            val page = repo.searchAllByCursor(null, size = 3, sort = null, cursor = null)
            assertEquals(3, page.data.size)
        }

        @Test
        fun `returns empty page when no subscribers match cursor`() = runBlocking {
            val repo = FakeSubscriberSearchRepository(mutableListOf())
            val page = repo.searchAllByCursor(null, size = 3, sort = null, cursor = null)
            assertTrue(page.data.isEmpty())
        }
    }
}
