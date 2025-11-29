package com.loomify.authentication.infrastructure

import com.loomify.authentication.domain.error.NotAuthenticatedUserException
import com.loomify.authentication.domain.error.UnknownAuthenticationException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/account-exceptions")
internal class AccountExceptionResource {
    @GetMapping("/not-authenticated")
    fun notAuthenticatedUser() {
        throw NotAuthenticatedUserException()
    }

    @GetMapping("/unknown-authentication")
    fun unknownAuthentication() {
        throw UnknownAuthenticationException()
    }
}
