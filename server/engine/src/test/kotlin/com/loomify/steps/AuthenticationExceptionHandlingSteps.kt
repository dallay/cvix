package com.loomify.steps

import com.loomify.ControllerIntegrationTest
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationExceptionHandlingSteps : ControllerIntegrationTest() {

    private lateinit var response: WebTestClient.ResponseSpec

    @When("a request is made to an endpoint that requires authentication without being authenticated")
    fun aRequestIsMadeToAnEndpointThatRequiresAuthenticationWithoutBeingAuthenticated() {
        response = webTestClient.get().uri("/api/account-exceptions/not-authenticated")
            .exchange()
    }

    @Then("the response should be a 401 Unauthorized error")
    fun theResponseShouldBeA401UnauthorizedError() {
        response.expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("error.http.401")
            .jsonPath("$.title").isEqualTo("not authenticated")
    }

    @When("a request is made to an endpoint that triggers an unknown authentication exception")
    fun aRequestIsMadeToAnEndpointThatTriggersAnUnknownAuthenticationException() {
        response = webTestClient.get().uri("/api/account-exceptions/unknown-authentication")
            .exchange()
    }

    @Then("the response should be a 5xx Server Error")
    fun theResponseShouldBeA5xxServerError() {
        response.expectStatus().is5xxServerError
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("$.message").isEqualTo("error.http.500")
            .jsonPath("$.title").isEqualTo("unknown authentication")
    }
}
