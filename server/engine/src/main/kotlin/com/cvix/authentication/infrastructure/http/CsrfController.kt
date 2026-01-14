package com.cvix.authentication.infrastructure.http

import com.cvix.AppConstants.Paths.API
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@Tag(
    name = "Security",
    description = "Security and CSRF protection endpoints",
)
@RestController
@RequestMapping(value = [API], produces = ["application/vnd.api.v1+json"])
class CsrfController {
    @GetMapping("/auth/csrf")
    fun getCsrfToken(): Mono<ResponseEntity<Map<String, String>>> =
        Mono.just(ResponseEntity.ok(mapOf("csrf" to "ok")))
}
