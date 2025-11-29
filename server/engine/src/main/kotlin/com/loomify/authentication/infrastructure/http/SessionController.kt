package com.loomify.authentication.infrastructure.http

import com.loomify.authentication.application.query.GetUserSessionQuery
import com.loomify.authentication.domain.UserSession
import com.loomify.authentication.domain.error.AuthenticationException
import com.loomify.authentication.domain.error.InvalidTokenException
import com.loomify.authentication.domain.error.MissingCookieException
import com.loomify.authentication.infrastructure.cookie.AuthCookieBuilder
import com.loomify.authentication.infrastructure.cookie.getCookie
import com.loomify.common.domain.bus.Mediator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api", produces = ["application/vnd.api.v1+json"])
class SessionController(private val mediator: Mediator) {

    @Operation(summary = "Get user session information")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "OK"),
        ApiResponse(responseCode = "401", description = "Unauthorized"),
        ApiResponse(responseCode = "500", description = "Internal server error"),
    )
    @GetMapping("/auth/session")
    suspend fun session(request: ServerHttpRequest): ResponseEntity<UserSession> {
        log.debug("Getting user session")
        return try {
            val accessToken = request.getCookie(AuthCookieBuilder.ACCESS_TOKEN).value
            ResponseEntity.ok(mediator.send(GetUserSessionQuery(accessToken)))
        } catch (e: InvalidTokenException) {
            handleException("Invalid token provided", HttpStatus.UNAUTHORIZED, e)
        } catch (e: AuthenticationException) {
            handleException("Authentication error", HttpStatus.UNAUTHORIZED, e)
        } catch (_: NoSuchElementException) {
            handleException("Missing access token cookie", HttpStatus.UNAUTHORIZED)
        } catch (e: IllegalArgumentException) {
            handleException("Invalid request data", HttpStatus.BAD_REQUEST, e)
        } catch (e: MissingCookieException) {
            handleException("Missing cookie", HttpStatus.UNAUTHORIZED, e)
        }
    }

    private fun handleException(
        message: String,
        status: HttpStatus,
        exception: Exception? = null
    ): ResponseEntity<UserSession> {
        exception?.let { log.warn(message, it) } ?: log.warn(message)
        return ResponseEntity.status(status).build()
    }

    companion object {
        private val log = LoggerFactory.getLogger(SessionController::class.java)
    }
}
