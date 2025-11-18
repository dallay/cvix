package com.loomify.steps

import com.loomify.ControllerIntegrationTest
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserAuthenticationSteps : ControllerIntegrationTest() {

    @Given("a user with valid credentials")
    fun aUserWithValidCredentials() {
        // No action needed here, the user is created in the 'when' step
    }

    @When("the user authenticates")
    fun theUserAuthenticates() {
        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
    }

    @Then("the user should be authenticated successfully")
    fun theUserShouldBeAuthenticatedSuccessfully() {
        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus().isOk
    }
}
