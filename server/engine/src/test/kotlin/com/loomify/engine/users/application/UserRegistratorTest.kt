package com.loomify.engine.users.application

import com.loomify.UnitTest
import com.loomify.common.domain.vo.credential.Credential
import com.loomify.common.domain.vo.credential.CredentialException
import com.loomify.common.domain.vo.email.Email
import com.loomify.common.domain.vo.name.FirstName
import com.loomify.common.domain.vo.name.LastName
import com.loomify.engine.users.application.register.UserRegistrator
import com.loomify.engine.users.domain.User
import com.loomify.engine.users.domain.UserCreator
import com.loomify.engine.users.domain.UserStoreException
import com.loomify.engine.users.domain.event.UserCreatedEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class UserRegistratorTest {
    private val faker = Faker()
    private val email = faker.internet().emailAddress()
    private val password = Credential.generateRandomCredentialPassword()
    private val firstname = faker.name().firstName()
    private val lastname = faker.name().lastName()

    @Nested
    inner class IntegrationTestsWithRealImplementations {
        private val userCreator = InMemoryUserRepository()
        private val eventPublisher = InMemoryEventPublisher<UserCreatedEvent>()
        private val userRegistrator = UserRegistrator(userCreator, eventPublisher)

        @Test
        fun `should register new user successfully and publish events`(): Unit = runTest {
            val userId = userRegistrator.registerNewUser(
                Email(email), Credential.create(password), FirstName(firstname),
                LastName(lastname),
            )

            // Verify UUID is returned
            assertTrue(userId.toString().isNotBlank(), "Expected valid UUID to be returned")

            val publishedEvents = eventPublisher.getEvents()
            assertFalse(publishedEvents.isEmpty(), "Expected at least one event to be published")
            assertTrue(
                publishedEvents.any {
                    it.javaClass == UserCreatedEvent::class.java
                },
                "Expected UserCreatedEvent to be published",
            )
        }

        @Test
        fun `should throw UserStoreException when trying to register user with existing email`(): Unit =
            runTest {
                // Register first user
                val firstUserId = userRegistrator.registerNewUser(
                    Email(email), Credential.create(password), FirstName(firstname),
                    LastName(lastname),
                )

                // Verify first registration returned UUID
                assertTrue(firstUserId.toString().isNotBlank(), "Expected valid UUID for first user")

                // When & Then - Attempting to register second user with same email should throw exception
                val exception = assertThrows<UserStoreException> {
                    userRegistrator.registerNewUser(
                        Email(email), Credential.create(password), FirstName("Another"),
                        LastName("User"),
                    )
                }
                assertTrue(
                    exception.message.contains("already exists"),
                    "Exception message should indicate user already exists",
                )
            }

        @Test
        fun `should throw CredentialException when password does not meet complexity requirements`(): Unit =
            runTest {
                // Given
                val weakPassword = "weak" // This will fail Credential validation

                // When & Then
                assertThrows<CredentialException> {
                    User.create(
                        UUID.randomUUID().toString(),
                        email,
                        firstname,
                        lastname,
                        weakPassword,
                    )
                }
            }

        @Test
        fun `should register multiple users with different emails successfully`(): Unit =
            runTest {
                val userId1 = userRegistrator.registerNewUser(
                    Email(faker.internet().emailAddress()),
                    Credential.create(Credential.generateRandomCredentialPassword()),
                    FirstName("John"),
                    LastName("Doe"),
                )
                val userId2 = userRegistrator.registerNewUser(
                    Email(faker.internet().emailAddress()),
                    Credential.create(Credential.generateRandomCredentialPassword()),
                    FirstName("Jane"),
                    LastName("Smith"),
                )
                val userId3 = userRegistrator.registerNewUser(
                    Email(faker.internet().emailAddress()),
                    Credential.create(Credential.generateRandomCredentialPassword()),
                    FirstName("Bob"),
                    LastName("Johnson"),
                )

                // Verify all registrations returned UUIDs
                assertTrue(userId1.toString().isNotBlank(), "Expected valid UUID for user 1")
                assertTrue(userId2.toString().isNotBlank(), "Expected valid UUID for user 2")
                assertTrue(userId3.toString().isNotBlank(), "Expected valid UUID for user 3")

                // Then - All users should be registered and events published
                val publishedEvents = eventPublisher.getEvents()
                assertTrue(
                    publishedEvents.size >= 3,
                    "Expected at least 3 events to be published for 3 users",
                )
            }
    }

    @Nested
    inner class UnitTestsWithMockedDependencies {
        private val userCreator: UserCreator = mockk()
        private val eventPublisher = InMemoryEventPublisher<UserCreatedEvent>()
        private val userRegistrator = UserRegistrator(userCreator, eventPublisher)

        private lateinit var testUser: User

        @BeforeEach
        fun setUp() {
            testUser =
                User.create(UUID.randomUUID().toString(), email, firstname, lastname, password)
        }

        @Test
        fun `should create user and publish domain events when registration succeeds`(): Unit =
            runTest {
                // Given
                val mockCreatedUser: User = mockk()
                val userCreatedEvent: UserCreatedEvent = mockk()
                val expectedUserId = UUID.randomUUID()
                val mockUserId: com.loomify.engine.users.domain.UserId = mockk()

                // Create objects once and reuse them to ensure the same instances are used
                val emailObj = Email(testUser.email.value)
                val credentialObj = Credential.create(password)
                val firstNameObj = FirstName(testUser.name?.firstName?.value ?: "")
                val lastNameObj = LastName(testUser.name?.lastName?.value ?: "")

                coEvery {
                    userCreator.create(
                        email = emailObj,
                        credential = credentialObj,
                        firstName = firstNameObj,
                        lastName = lastNameObj,
                    )
                } returns mockCreatedUser
                every { mockCreatedUser.pullDomainEvents() } returns listOf(userCreatedEvent)
                every { mockCreatedUser.id } returns mockUserId
                every { mockUserId.id } returns expectedUserId

                // When
                val actualUserId = userRegistrator.registerNewUser(
                    email = emailObj,
                    credential = credentialObj,
                    firstName = firstNameObj,
                    lastName = lastNameObj,
                )

                // Then - verify returned UUID
                assertEquals(expectedUserId, actualUserId, "Expected returned UUID to match mock")

                coVerify(exactly = 1) {
                    userCreator.create(
                        email = emailObj,
                        credential = credentialObj,
                        firstName = firstNameObj,
                        lastName = lastNameObj,
                    )
                }
                verify(exactly = 1) { mockCreatedUser.pullDomainEvents() }
                verify(exactly = 1) { mockCreatedUser.id }

                val publishedEvents = eventPublisher.getEvents()
                assertEquals(1, publishedEvents.size, "Expected exactly 1 event to be published")
                assertEquals(
                    userCreatedEvent,
                    publishedEvents[0],
                    "Expected UserCreatedEvent to be published",
                )
            }

        @Test
        fun `should propagate UserStoreException when user creation fails`(): Unit = runTest {
            // Given
            val errorMessage = "User with email: $email already exists"

            // Create objects once and reuse them to ensure the same instances are used
            val emailObj = Email(testUser.email.value)
            val credentialObj = Credential.create(password)
            val firstNameObj = FirstName(testUser.name?.firstName?.value ?: "")
            val lastNameObj = LastName(testUser.name?.lastName?.value ?: "")

            coEvery {
                userCreator.create(
                    email = emailObj,
                    credential = credentialObj,
                    firstName = firstNameObj,
                    lastName = lastNameObj,
                )
            } throws UserStoreException(errorMessage)

            // When & Then
            val exception = assertThrows<UserStoreException> {
                userRegistrator.registerNewUser(
                    email = emailObj,
                    credential = credentialObj,
                    firstName = firstNameObj,
                    lastName = lastNameObj,
                )
            }

            assertEquals(errorMessage, exception.message, "Exception message should match")
            coVerify(exactly = 1) {
                userCreator.create(
                    email = emailObj,
                    credential = credentialObj,
                    firstName = firstNameObj,
                    lastName = lastNameObj,
                )
            }

            // No events should be published on failure
            val publishedEvents = eventPublisher.getEvents()
            assertTrue(publishedEvents.isEmpty(), "No events should be published on failure")
        }
    }

    @Nested
    inner class EdgeCasesAndBoundaryTests {
        private val userCreator = InMemoryUserRepository()
        private val eventPublisher = InMemoryEventPublisher<UserCreatedEvent>()
        private val userRegistrator = UserRegistrator(userCreator, eventPublisher)

        @Test
        fun `should handle user with minimal valid data`(): Unit = runTest {
            // Given
            val minimalUser = User.create(
                id = UUID.randomUUID().toString(),
                email = "a@b.co", // Minimal valid email
                firstName = "A", // Single character
                lastName = "B", // Single character
                password = Credential.generateRandomCredentialPassword(), // Valid complex password
            )

            // When
            userRegistrator.registerNewUser(
                email = Email(minimalUser.email.value),
                credential = Credential.create(minimalUser.credentials.first().credentialValue.value),
                firstName = FirstName(minimalUser.name?.firstName?.value ?: ""),
                lastName = LastName(minimalUser.name?.lastName?.value ?: ""),
            )

            // Then - Should succeed without throwing exception
            val publishedEvents = eventPublisher.getEvents()
            assertFalse(publishedEvents.isEmpty(), "Expected at least one event to be published")
        }

        @Test
        fun `should handle user with maximum length data`(): Unit = runTest {
            // Given - Create realistic but long data that won't exceed validation limits
            val longFirstName = "A".repeat(50) // More reasonable length
            val longLastName = "B".repeat(50) // More reasonable length
            val maximalUser = User.create(
                id = UUID.randomUUID().toString(),
                email = "test@long-domain-name.com", // Valid but longer email
                firstName = longFirstName,
                lastName = longLastName,
                password = Credential.generateRandomCredentialPassword(),
            )

            // When
            userRegistrator.registerNewUser(
                email = Email(maximalUser.email.value),
                credential = Credential.create(maximalUser.credentials.first().credentialValue.value),
                firstName = FirstName(maximalUser.name?.firstName?.value ?: ""),
                lastName = LastName(maximalUser.name?.lastName?.value ?: ""),
            )

            // Then - Should succeed without throwing exception
            val publishedEvents = eventPublisher.getEvents()
            assertTrue(publishedEvents.isNotEmpty(), "Expected at least one event to be published")
            assertTrue(
                publishedEvents.any {
                    it.javaClass == UserCreatedEvent::class.java
                },
                "Expected UserCreatedEvent to be published",
            )
        }

        @Test
        fun `should handle special characters in user data`(): Unit = runTest {
            // Given
            val specialCharsUser = User.create(
                id = UUID.randomUUID().toString(),
                email = "test+tag@example-domain.co.uk",
                firstName = "José-María",
                lastName = "O'Connor-Smith",
                password = Credential.generateRandomCredentialPassword(),
            )

            // When
            userRegistrator.registerNewUser(
                email = Email(specialCharsUser.email.value),
                credential = Credential.create(specialCharsUser.credentials.first().credentialValue.value),
                firstName = FirstName(specialCharsUser.name?.firstName?.value ?: ""),
                lastName = LastName(specialCharsUser.name?.lastName?.value ?: ""),
            )

            // Then - Should succeed without throwing exception
            val publishedEvents = eventPublisher.getEvents()
            assertFalse(publishedEvents.isEmpty(), "Expected at least one event to be published")
        }

        @Test
        fun `should handle unicode characters in user data`(): Unit = runTest {
            // Given
            val unicodeUser = User.create(
                id = UUID.randomUUID().toString(),
                email = "test@example.com", // Use regular email to avoid potential unicode email issues
                firstName = "张",
                lastName = "三",
                password = Credential.generateRandomCredentialPassword(),
            )

            // When
            userRegistrator.registerNewUser(
                email = Email(unicodeUser.email.value),
                credential = Credential.create(unicodeUser.credentials.first().credentialValue.value),
                firstName = FirstName(unicodeUser.name?.firstName?.value ?: ""),
                lastName = LastName(unicodeUser.name?.lastName?.value ?: ""),
            )

            // Then - Should succeed without throwing exception
            val publishedEvents = eventPublisher.getEvents()
            assertFalse(publishedEvents.isEmpty(), "Expected at least one event to be published")
        }

        @Test
        fun `should handle user with empty domain events list`(): Unit = runTest {
            // Given
            val userCreatorMock: UserCreator = mockk()
            val mockUser: User = mockk()
            val registrator = UserRegistrator(userCreatorMock, eventPublisher)
            val expectedUserId = UUID.randomUUID()
            val mockUserId: com.loomify.engine.users.domain.UserId = mockk()

            val testUser =
                User.create(UUID.randomUUID().toString(), email, firstname, lastname, password)
            // Create the credential once and reuse it to ensure the same object identity
            val credential = Credential.create(password)
            val emailObj = Email(testUser.email.value)
            val firstNameObj = FirstName(testUser.name?.firstName?.value ?: "")
            val lastNameObj = LastName(testUser.name?.lastName?.value ?: "")

            coEvery {
                userCreatorMock.create(
                    email = emailObj,
                    credential = credential,
                    firstName = firstNameObj,
                    lastName = lastNameObj,
                )
            } returns mockUser
            every { mockUser.pullDomainEvents() } returns emptyList()
            every { mockUser.id } returns mockUserId
            every { mockUserId.id } returns expectedUserId

            // When
            val actualUserId = registrator.registerNewUser(
                email = emailObj,
                credential = credential,
                firstName = firstNameObj,
                lastName = lastNameObj,
            )

            // Then - verify returned UUID
            assertEquals(expectedUserId, actualUserId, "Expected returned UUID to match mock")

            val publishedEvents = eventPublisher.getEvents()
            assertTrue(
                publishedEvents.isEmpty(),
                "No events should be published when domain events list is empty",
            )
            coVerify(exactly = 1) {
                userCreatorMock.create(
                    email = emailObj,
                    credential = credential,
                    firstName = firstNameObj,
                    lastName = lastNameObj,
                )
            }
            verify(exactly = 1) { mockUser.pullDomainEvents() }
            verify(exactly = 1) { mockUser.id }
        }
    }
}
