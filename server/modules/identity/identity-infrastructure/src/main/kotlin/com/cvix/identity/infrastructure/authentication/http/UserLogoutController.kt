package com.cvix.identity.infrastructure.authentication.http

import com.cvix.common.domain.bus.Mediator
import com.cvix.identity.application.authentication.logout.UserLogoutCommand
import com.cvix.identity.domain.authentication.error.MissingCookieException
import com.cvix.identity.infrastructure.authentication.cookie.AuthCookieBuilder
import com.cvix.identity.infrastructure.authentication.cookie.getCookie
import com.cvix.spring.boot.ApiController
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpCookie
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * This class is a REST controller that handles HTTP requests related to user logout.
 * It extends the ApiController class and uses the Mediator pattern for handling commands.
 *
 * @created 15/8/24
 */
@Tag(
    name = "Authentication",
    description = "User authentication and session management endpoints",
)
@RestController
@RequestMapping(value = ["/api"], produces = ["application/vnd.api.v1+json"])
class UserLogoutController(
    mediator: Mediator,
) : ApiController(mediator) {

    /**
     * This function handles the POST HTTP request for logging out a user.
     * It dispatches a UserLogoutCommand to log out the user.
     * The function is a suspend function, meaning it is designed to be used with Kotlin coroutines.
     *
     * @param request The HTTP request containing the cookies of the user to be logged out.
     * @param response The HTTP response where cookies will be cleared.
     * @throws MissingCookieException if the required cookies are not present.
     */
    @Operation(summary = "Logout a user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User logged out successfully"),
        ApiResponse(responseCode = "400", description = "Bad request, missing cookies"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.OK)
    suspend fun logout(
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ) {
        log.debug("Attempting to log out user")

        val refreshToken: HttpCookie = request.getCookie(AuthCookieBuilder.REFRESH_TOKEN)
        dispatch(UserLogoutCommand(refreshToken.value))

        AuthCookieBuilder.clearCookies(response)
        log.debug("User logged out successfully")
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(UserLogoutController::class.java)
    }
}
