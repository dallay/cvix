package com.loomify.engine.authentication.infrastructure.http

import com.loomify.engine.AppConstants.Paths.API
import org.springframework.http.ResponseEntity
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Controller to expose CSRF token for Single Page Applications.
 * This endpoint allows the frontend to obtain the CSRF token before making state-changing requests.
 */
@RestController
@RequestMapping(value = [API], produces = ["application/vnd.api.v1+json"])
class CsrfController {

    /**
     * Endpoint to obtain the CSRF token.
     * The token is automatically set as a cookie by the CookieCsrfFilter.
     * This endpoint simply triggers the filter to set the cookie.
     *
     * @param token The CSRF token (injected by Spring Security)
     * @return Empty response with CSRF token set as cookie
     */
    @GetMapping("/auth/csrf")
    fun getCsrfToken(token: Mono<CsrfToken>): Mono<ResponseEntity<Void>> =
        token.map { ResponseEntity.ok().build() }
}
