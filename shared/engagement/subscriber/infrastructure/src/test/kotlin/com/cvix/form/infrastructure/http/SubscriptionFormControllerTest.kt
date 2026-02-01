package com.cvix.form.infrastructure.http

import com.cvix.ControllerTest
import com.cvix.config.WorkspaceContextWebFilter
import com.cvix.form.application.SubscriberFormResponse
import com.cvix.form.application.SubscriberFormStub
import com.cvix.form.application.create.CreateSubscriberFormCommand
import com.cvix.form.application.delete.DeleteSubscriberFormCommand
import com.cvix.form.application.details.DetailSubscriberFormQuery
import com.cvix.form.application.find.SubscriberFormFinder
import com.cvix.form.application.update.UpdateSubscriberFormCommand
import com.cvix.form.infrastructure.http.request.CreateSubscriberFormRequest
import com.cvix.form.infrastructure.http.request.UpdateSubscriberFormRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.test.web.reactive.server.WebTestClient

internal class SubscriptionFormControllerTest : ControllerTest() {
    private lateinit var createController: CreateSubscriptionFormController
    private lateinit var updateController: UpdateSubscriptionFormController
    private lateinit var deleteController: DeleteSubscriptionFormController
    private lateinit var getController: GetSubscriptionFormController
    private lateinit var searchController: SearchSubscriptionFormsController
    private val formFinder: SubscriberFormFinder = mockk()
    override lateinit var webTestClient: WebTestClient

    @BeforeEach
    override fun setUp() {
        super.setUp()
        every {
            messageSource.getMessage("subscription-form.create.success", null, any())
        } returns "Form created successfully!"
        every {
            messageSource.getMessage("subscription-form.update.success", null, any())
        } returns "Form updated successfully!"
        every {
            messageSource.getMessage("subscription-form.delete.success", null, any())
        } returns "Form deleted successfully!"

        createController = CreateSubscriptionFormController(mediator, messageSource)
        updateController = UpdateSubscriptionFormController(mediator, messageSource)
        deleteController = DeleteSubscriptionFormController(mediator, messageSource)
        getController = GetSubscriptionFormController(mediator, formFinder)
        searchController = SearchSubscriptionFormsController(mediator)

        webTestClient = WebTestClient.bindToController(
            createController,
            updateController,
            deleteController,
            getController,
            searchController,
        )
            .webFilter<WebTestClient.ControllerSpec>(WorkspaceContextWebFilter())
            .controllerAdvice(com.cvix.controllers.GlobalExceptionHandler(messageSource))
            .configureClient()
            .build()
            .mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf())
            .mutateWith(
                org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt()
                    .jwt { jwt ->
                        jwt.subject(userId.toString())
                            .claim("preferred_username", "test-user")
                            .claim("roles", listOf("ROLE_USER"))
                    }
                    .authorities(AuthorityUtils.createAuthorityList("ROLE_USER")),
            )
    }

    @Test
    fun `should create form successfully`() {
        val request = CreateSubscriberFormRequest(
            name = "Test Form",
            description = "Desc",
            headerTitle = "Header",
            inputPlaceholder = "email@example.com",
            submitButtonText = "Join",
            buttonColor = "#000000",
            pageBackgroundColor = "#ffffff",
            backgroundColor = "#ffffff",
            textColor = "#000000",
            buttonTextColor = "#ffffff",
            inputTextColor = "#000000",
            borderColor = "#000000",
            borderStyle = "solid",
            shadow = "none",
            borderThickness = 0,
            width = "auto",
            height = "auto",
            horizontalAlignment = "center",
            verticalAlignment = "center",
            padding = 16,
            gap = 16,
            cornerRadius = 8,
        )

        coEvery { mediator.send(any<CreateSubscriberFormCommand>()) } returns Unit

        webTestClient.post()
            .uri("/api/subscription-forms")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept", "application/vnd.api.v1+json")
            .header("X-Workspace-Id", workspaceId.toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.message").isEqualTo("Form created successfully!")

        coVerify(exactly = 1) { mediator.send(any<CreateSubscriberFormCommand>()) }
    }

    @Test
    fun `should update form successfully`() {
        val formId = UUID.randomUUID()
        val request = UpdateSubscriberFormRequest(
            name = "Updated Form",
            description = "Desc",
            headerTitle = "Header",
            inputPlaceholder = "email@example.com",
            submitButtonText = "Join",
            submittingButtonText = "Joining...",
            buttonColor = "#000000",
            pageBackgroundColor = "#ffffff",
            backgroundColor = "#ffffff",
            textColor = "#000000",
            buttonTextColor = "#ffffff",
            inputTextColor = "#000000",
            borderColor = "#000000",
            borderStyle = "solid",
            shadow = "none",
            borderThickness = 0,
            width = "auto",
            height = "auto",
            horizontalAlignment = "center",
            verticalAlignment = "center",
            padding = 16,
            gap = 16,
            cornerRadius = 8,
        )

        coEvery { mediator.send(any<UpdateSubscriberFormCommand>()) } returns Unit

        webTestClient.put()
            .uri("/api/subscription-forms/$formId")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Accept", "application/vnd.api.v1+json")
            .header("X-Workspace-Id", workspaceId.toString())
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Form updated successfully!")

        coVerify(exactly = 1) { mediator.send(any<UpdateSubscriberFormCommand>()) }
    }

    @Test
    fun `should delete form successfully`() {
        val formId = UUID.randomUUID()

        coEvery { mediator.send(any<DeleteSubscriberFormCommand>()) } returns Unit

        webTestClient.delete()
            .uri("/api/subscription-forms/$formId")
            .header("Accept", "application/vnd.api.v1+json")
            .header("X-Workspace-Id", workspaceId.toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.message").isEqualTo("Form deleted successfully!")

        coVerify(exactly = 1) { mediator.send(any<DeleteSubscriberFormCommand>()) }
    }

    @Test
    fun `should get form details`() {
        val formId = UUID.randomUUID()
        val form = SubscriberFormStub.randomForm()
        val response = SubscriberFormResponse.from(form)

        coEvery { mediator.send(any<DetailSubscriberFormQuery>()) } returns response

        webTestClient.get()
            .uri("/api/subscription-forms/$formId")
            .header("Accept", "application/vnd.api.v1+json")
            .header("X-Workspace-Id", workspaceId.toString())
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(response.id)
            .jsonPath("$.name").isEqualTo(response.name)
    }
}
