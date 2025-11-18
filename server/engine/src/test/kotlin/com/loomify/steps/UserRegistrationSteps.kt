package com.loomify.steps

import com.loomify.common.domain.vo.credential.Credential
import com.loomify.engine.config.InfrastructureTestContainers
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import net.datafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.concurrent.atomic.AtomicInteger

private const val ENDPOINT = "/api/auth/register"

data class TestContext(
    var webTestClient: WebTestClient,
    val faker: Faker = Faker(),
    val ipCounter: AtomicInteger = AtomicInteger(0),
    var requestBody: String? = null,
    var response: WebTestClient.ResponseSpec? = null
) {
    fun uniqueIp(): String = "192.168.1.${ipCounter.incrementAndGet()}"
}

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserRegistrationSteps : InfrastructureTestContainers() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private lateinit var testContext: TestContext

    @Given("a new user with valid registration details")
    fun aNewUserWithValidRegistrationDetails() {
        testContext = TestContext(webTestClient)
        testContext.requestBody = """
            {
                "email": "${testContext.faker.internet().emailAddress()}",
                "password": "${Credential.generateRandomCredentialPassword()}",
                "firstname": "${testContext.faker.name().firstName()}",
                "lastname": "${testContext.faker.name().lastName()}"
            }
        """.trimIndent()
    }

    @When("the user submits the registration form")
    fun theUserSubmitsTheRegistrationForm() {
        testContext.response = testContext.webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", testContext.uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testContext.requestBody!!)
            .exchange()
    }

    @Then("the registration should be successful")
    fun theRegistrationShouldBeSuccessful() {
        testContext.response!!
            .expectStatus().isCreated
            .expectBody()
            .consumeWith {
                println(it.responseBody)
            }
    }

    @When("the user submits the registration form without a CSRF token")
    fun theUserSubmitsTheRegistrationFormWithoutACSRFToken() {
        testContext.response = testContext.webTestClient
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", testContext.uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testContext.requestBody!!)
            .exchange()
    }

    @Then("the registration should be forbidden")
    fun theRegistrationShouldBeForbidden() {
        testContext.response!!.expectStatus().isForbidden
    }

    @Given("a user has already registered with an email")
    fun aUserHasAlreadyRegisteredWithAnEmail() {
        testContext = TestContext(webTestClient)
        val email = testContext.faker.internet().emailAddress()
        testContext.requestBody = """
            {
                "email": "$email",
                "password": "${Credential.generateRandomCredentialPassword()}",
                "firstname": "${testContext.faker.name().firstName()}",
                "lastname": "${testContext.faker.name().lastName()}"
            }
        """.trimIndent()

        testContext.webTestClient
            .mutateWith(csrf())
            .post()
            .uri(ENDPOINT)
            .header("X-Forwarded-For", testContext.uniqueIp())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(testContext.requestBody!!)
            .exchange()
            .expectStatus().isCreated
    }

    @When("another user tries to register with the same email")
    fun anotherUserTriesToRegisterWithTheSameEmail() {
        theUserSubmitsTheRegistrationForm()
    }

    @Then("the registration should fail")
    fun theRegistrationShouldFail() {
        testContext.response!!.expectStatus().isBadRequest
    }

    @Given("a user provides an invalid email")
    fun aUserProvidesAnInvalidEmail() {
        testContext = TestContext(webTestClient)
        testContext.requestBody = """
            {
                "email": "invalid-email",
                "password": "${Credential.generateRandomCredentialPassword()}",
                "firstname": "${testContext.faker.name().firstName()}",
                "lastname": "${testContext.faker.name().lastName()}"
            }
        """.trimIndent()
    }

    @Given("a user provides a password {string}")
    fun aUserProvidesAPassword(password: String) {
        testContext = TestContext(webTestClient)
        testContext.requestBody = """
            {
                "email": "${testContext.faker.internet().emailAddress()}",
                "password": "$password",
                "firstname": "${testContext.faker.name().firstName()}",
                "lastname": "${testContext.faker.name().lastName()}"
            }
        """.trimIndent()
    }

    @Given("a user provides an empty firstname")
    fun aUserProvidesAnEmptyFirstname() {
        testContext = TestContext(webTestClient)
        testContext.requestBody = """
            {
                "email": "${testContext.faker.internet().emailAddress()}",
                "password": "${Credential.generateRandomCredentialPassword()}",
                "firstname": "",
                "lastname": "${testContext.faker.name().lastName()}"
            }
        """.trimIndent()
    }

    @Given("a user provides an empty lastname")
    fun aUserProvidesAnEmptyLastname() {
        testContext = TestContext(webTestClient)
        testContext.requestBody = """
            {
                "email": "${testContext.faker.internet().emailAddress()}",
                "password": "${Credential.generateRandomCredentialPassword()}",
                "firstname": "${testContext.faker.name().firstName()}",
                "lastname": ""
            }
        """.trimIndent()
    }
}
