package com.cvix.workspace.domain

import java.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WorkspaceTest {

    private lateinit var workspace: Workspace
    private lateinit var workspaceId: UUID
    private lateinit var ownerId: UUID
    private lateinit var memberId: UUID

    @BeforeEach
    fun setUp() {
        workspaceId = UUID.randomUUID()
        ownerId = UUID.randomUUID()
        memberId = UUID.randomUUID()
        workspace = Workspace.create(
            workspaceId,
            "Test Workspace",
            "Workspace description",
            ownerId,
        )
    }

    @Test
    fun `create workspace`() {
        assertNotNull(workspace)
        assertEquals(workspaceId, workspace.id.id)
        assertEquals("Test Workspace", workspace.name)
        assertEquals("Workspace description", workspace.description)
        assertEquals(ownerId, workspace.ownerId.id)
        assertNotNull(workspace.createdAt)
        assertNotNull(workspace.updatedAt)
    }

    @Test
    fun `should include owner as member`() {
        assertTrue(workspace.isMember(ownerId))
    }

    @Test
    fun `should add new member and report success`() {
        val added = workspace.addMember(memberId)
        assertTrue(added)
        assertTrue(workspace.isMember(memberId))
    }

    @Test
    fun `should not add existing member`() {
        workspace.addMember(memberId)
        val addedAgain = workspace.addMember(memberId)
        assertFalse(addedAgain)
    }

    @Test
    fun `should remove non-owner member and report success`() {
        workspace.addMember(memberId)
        val removed = workspace.removeMember(memberId)
        assertTrue(removed)
        assertFalse(workspace.isMember(memberId))
    }

    @Test
    fun `should not remove non-existent member`() {
        val removed = workspace.removeMember(UUID.randomUUID())
        assertFalse(removed)
    }

    @Test
    fun `should not remove owner`() {
        val removedOwner = workspace.removeMember(ownerId)
        assertFalse(removedOwner)
        assertTrue(workspace.isMember(ownerId))
    }

    @Test
    fun `isMember and isOwner predicates`() {
        workspace.addMember(memberId)
        assertTrue(workspace.isMember(memberId))
        assertTrue(workspace.isOwner(ownerId))
        assertFalse(workspace.isOwner(memberId))
    }

    @Test
    fun `update workspace`() {
        workspace.update("New Name")
        assertEquals("New Name", workspace.name)
        assertNotNull(workspace.updatedAt)
    }

    @Test
    fun `update workspace name and description`() {
        workspace.update("New Name", "New Description")
        assertEquals("New Name", workspace.name)
        assertEquals("New Description", workspace.description)
        assertNotNull(workspace.updatedAt)
    }

    @Test
    fun `should create workspace when name length is exactly max`() {
        val maxName = "a".repeat(Workspace.NAME_MAX_LENGTH)
        val ws = Workspace.create(UUID.randomUUID(), maxName, null, ownerId)
        assertEquals(maxName, ws.name)
    }

    @Test
    fun `should trim name and reject if blank after trimming`() {
        assertThrows(WorkspaceException::class.java) {
            Workspace.create(UUID.randomUUID(), "   \t  ", null, ownerId)
        }
    }

    @Test
    fun `should throw when name length exceeds max`() {
        val tooLong = "a".repeat(Workspace.NAME_MAX_LENGTH + 1)
        assertThrows(WorkspaceException::class.java) {
            Workspace.create(UUID.randomUUID(), tooLong, null, ownerId)
        }
    }
}
