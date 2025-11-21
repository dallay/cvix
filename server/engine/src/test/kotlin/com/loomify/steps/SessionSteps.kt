package com.loomify.steps

import com.loomify.ControllerIntegrationTest
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SessionSteps : ControllerIntegrationTest() {

    @When("the user requests their session information")
    fun theUserRequestsTheirSessionInformation() {
        webTestClient.get()
            .uri("/api/auth/session")
            .exchange()
    }

    @Then("the session information should be returned")
    fun theSessionInformationShouldBeReturned() {
        webTestClient.get()
            .uri("/api/auth/session")
            .exchange()
            .expectStatus().isOk
    }
}
