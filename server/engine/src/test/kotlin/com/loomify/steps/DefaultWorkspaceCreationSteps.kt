package com.loomify.steps

import com.loomify.IntegrationTest
import com.loomify.common.domain.bus.event.EventPublisher
import com.loomify.engine.config.InfrastructureTestContainers
import com.loomify.engine.users.domain.UserId
import com.loomify.engine.users.domain.event.UserCreatedEvent
import com.loomify.engine.workspace.domain.WorkspaceFinderRepository
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import net.datafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@IntegrationTest
class DefaultWorkspaceCreationSteps : InfrastructureTestContainers() {

    @Autowired
    private lateinit var eventPublisher: EventPublisher<UserCreatedEvent>

    @Autowired
    private lateinit var workspaceFinderRepository: WorkspaceFinderRepository

    private val faker = Faker()
    private val userId = "efc4b2b8-08be-4020-93d5-f795762bf5c9"
    private lateinit var userCreatedEvent: UserCreatedEvent

    @Given("a new user is created with a first and last name")
    @Sql("/db/user/users.sql")
    fun aNewUserIsCreatedWithAFirstAndLastName() {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName()
        )
    }

    @When("the user created event is published")
    fun theUserCreatedEventIsPublished() = runTest {
        eventPublisher.publish(userCreatedEvent)
    }

    @Then("a default workspace should be created with the user's full name")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun aDefaultWorkspaceShouldBeCreatedWithTheUserSFullName() = runTest {
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1

            val workspace = workspaces.first()
            workspace.name shouldBe "${userCreatedEvent.firstName} ${userCreatedEvent.lastName}'s Workspace"
            workspace.description shouldBe "Default workspace created automatically upon user registration"
            workspace.ownerId.value.toString() shouldBe userId
        }
    }

    @Given("a new user is created with only a first name")
    @Sql("/db/user/users.sql")
    fun aNewUserIsCreatedWithOnlyAFirstName() {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = null
        )
    }

    @Then("a default workspace should be created with the user's first name")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun aDefaultWorkspaceShouldBeCreatedWithTheUserSFirstName() = runTest {
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1
            workspaces.first().name shouldBe "${userCreatedEvent.firstName}'s Workspace"
        }
    }

    @Given("a new user is created with only a last name")
    @Sql("/db/user/users.sql")
    fun aNewUserIsCreatedWithOnlyALastName() {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = null,
            lastName = faker.name().lastName()
        )
    }

    @Then("a default workspace should be created with the user's last name")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun aDefaultWorkspaceShouldBeCreatedWithTheUserSLastName() = runTest {
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1
            workspaces.first().name shouldBe "${userCreatedEvent.lastName}'s Workspace"
        }
    }

    @Given("a new user is created with no name")
    @Sql("/db/user/users.sql")
    fun aNewUserIsCreatedWithNoName() {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = null,
            lastName = null
        )
    }

    @Then("a default workspace should be created with the name {string}")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun aDefaultWorkspaceShouldBeCreatedWithTheName(workspaceName: String) = runTest {
        eventually(5.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)).first().name shouldBe workspaceName
        }
    }

    @Given("a user already has a workspace")
    @Sql("/db/user/users.sql")
    fun aUserAlreadyHasAWorkspace() = runTest {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName()
        )
        eventPublisher.publish(userCreatedEvent)
        eventually(5.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)) shouldHaveSize 1
        }
    }

    @When("a user created event is published for that user")
    fun aUserCreatedEventIsPublishedForThatUser() = runTest {
        eventPublisher.publish(userCreatedEvent)
    }

    @Then("no new workspace should be created")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun noNewWorkspaceShouldBeCreated() = runTest {
        eventually(2.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)) shouldHaveSize 1
        }
    }

    @Given("a new user is created with a name containing special characters and whitespace")
    @Sql("/db/user/users.sql")
    fun aNewUserIsCreatedWithANameContainingSpecialCharactersAndWhitespace() {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = "  José María  ",
            lastName = "  González-López  "
        )
    }

    @Then("a default workspace should be created with a trimmed and formatted name")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun aDefaultWorkspaceShouldBeCreatedWithATrimmedAndFormattedName() = runTest {
        eventually(5.seconds) {
            val workspaces = workspaceFinderRepository.findByOwnerId(UserId(userId))
            workspaces shouldHaveSize 1
            workspaces.first().name shouldBe "José María González-López's Workspace"
        }
    }

    @Given("duplicate user created events are published concurrently")
    @Sql("/db/user/users.sql")
    fun duplicateUserCreatedEventsArePublishedConcurrently() {
        userCreatedEvent = UserCreatedEvent(
            id = userId,
            email = faker.internet().emailAddress(),
            firstName = faker.name().firstName(),
            lastName = faker.name().lastName()
        )
    }

    @When("the events are processed")
    fun theEventsAreProcessed() = runTest {
        awaitAll(
            async { eventPublisher.publish(userCreatedEvent) },
            async { eventPublisher.publish(userCreatedEvent) }
        )
    }

    @Then("only one workspace should be created")
    @Sql("/db/user/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun onlyOneWorkspaceShouldBeCreated() = runTest {
        eventually(5.seconds) {
            workspaceFinderRepository.findByOwnerId(UserId(userId)) shouldHaveSize 1
        }
    }
}
