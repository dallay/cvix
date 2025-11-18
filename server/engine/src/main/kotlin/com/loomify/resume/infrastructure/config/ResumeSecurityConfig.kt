package com.loomify.resume.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher

private const val RESUME_ORDER = 2

private const val TEMPLATE_ORDER = 3

@Configuration
@EnableWebFluxSecurity
class ResumeSecurityConfig {

    @Bean
    @Order(RESUME_ORDER)
    fun resumeSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .securityMatcher(
                PathPatternParserServerWebExchangeMatcher("/api/resumes/**"),
            )
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/resumes/**").authenticated()
                    .anyExchange().permitAll()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .csrf { it.disable() }
            .build()

    @Bean
    @Order(TEMPLATE_ORDER)
    fun templateSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .securityMatcher(
                PathPatternParserServerWebExchangeMatcher("/api/templates/**"),
            )
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/api/templates/**").permitAll()
                    .anyExchange().permitAll()
            }
            .csrf { it.disable() }
            .build()
}
