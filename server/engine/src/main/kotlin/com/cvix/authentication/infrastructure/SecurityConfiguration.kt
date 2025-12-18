package com.cvix.authentication.infrastructure

import com.cvix.authentication.domain.Role
import com.cvix.authentication.infrastructure.csrf.SpaCsrfTokenRequestHandler
import com.cvix.authentication.infrastructure.filter.CookieCsrfFilter
import com.cvix.authentication.infrastructure.filter.JwtCookieOrHeaderFilter
import com.cvix.common.domain.Generated
import com.cvix.ratelimit.infrastructure.filter.RateLimitingFilter
import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.csrf.CsrfServerLogoutHandler
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

@Suppress("MaxLineLength")
private const val POLICY =
    "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"

/**
 * Configuration class for setting up security in a Spring WebFlux application.
 *
 * This class is responsible for configuring the security settings in the application.
 * It also configures the security filter chain that will be used to protect the application.
 * @param applicationSecurityProperties the application security properties
 * @since 1.0.0
 * @see EnableWebFluxSecurity for enabling Spring Security in a WebFlux application
 * @see EnableReactiveMethodSecurity for enabling Spring Security method security in a WebFlux application
 * @see WebSecurityCustomizer for customizing the WebSecurity configuration
 * @see SecurityWebFilterChain for configuring the security filter chain
 * @see ServerHttpSecurity for configuring the security filter chain
 * @see ReactiveClientRegistrationRepository for managing OAuth 2.0 Client Registration
 * @see ReactiveJwtDecoder for decoding a JSON Web Token (JWT) from a Bearer Token request
 * @see WebClient for performing HTTP requests
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfiguration(
    val applicationSecurityProperties: ApplicationSecurityProperties,
    private val rateLimitingFilter: RateLimitingFilter
) {
    @Value("\${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private val issuerUri: String? = null

    /**
     * Returns a [CorsConfigurationSource] object that is configured based on the properties defined in
     * the applicationSecurityProperties. This method is annotated with @Bean, indicating that it should be
     * treated as a bean and managed by Spring framework.
     *
     * @return A [CorsConfigurationSource] object that is configured based on the applicationSecurityProperties.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = applicationSecurityProperties.cors.allowedOrigins
        configuration.allowedMethods = applicationSecurityProperties.cors.allowedMethods
        configuration.allowedHeaders = applicationSecurityProperties.cors.allowedHeaders
        configuration.exposedHeaders = applicationSecurityProperties.cors.exposedHeaders
        configuration.allowCredentials = applicationSecurityProperties.cors.allowCredentials
        configuration.maxAge = applicationSecurityProperties.cors.maxAge
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    /**
     * Builds a SecurityWebFilterChain for the provided ServerHttpSecurity instance.
     *
     * @param http The ServerHttpSecurity instance to configure the filter chain.
     * @return The configured SecurityWebFilterChain.
     */
    @Bean
    fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // @formatter:off
        return http
            .securityMatcher(
                serverWebExchangeMatcher(),
            )
            .csrf {
                    csrf ->
                csrf
                    .csrfTokenRepository(
                        CookieServerCsrfTokenRepository.withHttpOnlyFalse().apply {
                            // Set header name to X-XSRF-TOKEN to match Axios default behavior
                            setHeaderName("X-XSRF-TOKEN")
                            // Only set cookie domain if domain is configured and not localhost
                            if (applicationSecurityProperties.domain.isNotEmpty() &&
                                applicationSecurityProperties.domain != "localhost"
                            ) {
                                setCookieCustomizer {
                                    it.domain(
                                        if (applicationSecurityProperties.domain.startsWith(".")) {
                                            applicationSecurityProperties.domain
                                        } else {
                                            "." + applicationSecurityProperties.domain
                                        },
                                    )
                                }
                            }
                        },
                    )
                    .csrfTokenRequestHandler(SpaCsrfTokenRequestHandler())
            }
            .cors {
                    cors ->
                cors.configurationSource(corsConfigurationSource())
            }
            .addFilterAt(CookieCsrfFilter(applicationSecurityProperties), SecurityWebFiltersOrder.REACTOR_CONTEXT)
            .addFilterAt(JwtCookieOrHeaderFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterBefore(rateLimitingFilter, SecurityWebFiltersOrder.HTTP_BASIC)
            .redirectToHttps {
                    httpsRedirect ->
                httpsRedirect.httpsRedirectWhen {
                    it.request.headers.containsKey("X-Forwarded-Proto")
                }
            }
            .headers { headers ->
                headers.contentSecurityPolicy { csp ->
                    csp.policyDirectives(applicationSecurityProperties.contentSecurityPolicy)
                }
                headers.referrerPolicy { referrerPolicy ->
                    referrerPolicy.policy(
                        ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN,
                    )
                }
                headers.permissionsPolicy { permissions -> permissions.policy(POLICY) }
                headers.contentTypeOptions { }
                headers.frameOptions { frame ->
                    frame.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY)
                }
            }
            .authorizeExchange {
                    auth ->
                configureAuthorization(auth)
            }
            .oauth2Login(withDefaults())
            .oauth2Client(withDefaults())
            .oauth2ResourceServer {
                    oauth2 ->
                oauth2.jwt {
                        jwt ->
                    jwt.jwtAuthenticationConverter(authenticationConverter())
                }
            }.logout {
                    logout ->
                logout.logoutHandler(CsrfServerLogoutHandler(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
            }
            .build()
        // @formatter:on
    }

    private fun configureAuthorization(auth: ServerHttpSecurity.AuthorizeExchangeSpec) {
        auth
            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .pathMatchers(
                "/", "/api/health-check", "/api/auth/register",
                "/api/auth/refresh-token", "/api/auth/login", "/api/auth/logout",
                "/api/auth/csrf",
                "/api/auth/federated/**", "/oauth2/**", "/login/oauth2/**",
                "actuator/info",
            ).permitAll()
            .pathMatchers(
                "/swagger-ui/**", "/webjars/**", "/api-docs/**", "/swagger-ui.html",
                "/v3/api-docs/**", "/v3/api-docs.yaml",
            ).permitAll()
            .pathMatchers(HttpMethod.POST, "/api/waitlist").permitAll()
            .pathMatchers("/actuator/**").authenticated()
            .pathMatchers("/api/**").authenticated()
            .pathMatchers("/management/health").permitAll()
            .pathMatchers("/management/info").permitAll()
            .pathMatchers("/management/prometheus").permitAll()
            .pathMatchers("/management/**").hasAuthority(Role.ADMIN.key())
    }

    private fun serverWebExchangeMatcher() = NegatedServerWebExchangeMatcher(
        OrServerWebExchangeMatcher(
            ServerWebExchangeMatchers.pathMatchers(
                "/app/**",
                "/_app/**",
                "/i18n/**",
                "/img/**",
                "/content/**",
                "/swagger-ui/**",
                "/webjars/**",
                "/api-docs/**",
                "/v3/api-docs/**",
                "/test/**",
            ),
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.OPTIONS, "/**"),
        ),
    )

    /**
     * Converts a Jwt token into a Mono of [AbstractAuthenticationToken],
     * using a [ReactiveJwtAuthenticationConverterAdapter].
     *
     * @return Converter<Jwt, Mono<AbstractAuthenticationToken>> the authentication converter.
     */
    fun authenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtGrantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
        val jwtAuthenticationConverter = ReactiveJwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
            ReactiveJwtGrantedAuthoritiesConverterAdapter(jwtGrantedAuthoritiesConverter),
        )

        return jwtAuthenticationConverter
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a [GrantedAuthoritiesMapper] that maps groups from the IdP to Spring Security Authorities.
     */
    @Bean
    fun userAuthoritiesMapper(): GrantedAuthoritiesMapper {
        return GrantedAuthoritiesMapper { authorities ->
            val mappedAuthorities = HashSet<GrantedAuthority>()

            authorities.forEach { authority ->
                // Check for OidcUserAuthority because Spring Security 5.2 returns
                // each scope as a GrantedAuthority, which we don't care about.
                if (authority is OidcUserAuthority) {
                    mappedAuthorities.addAll(Claims.extractAuthorityFromClaims(authority.userInfo.claims))
                }
            }
            mappedAuthorities
        }
    }

    /**
     * Creates a ReactiveJwtDecoder instance for decoding JWT tokens.
     *
     * @param clientRegistrationRepository The repository containing client registrations.
     * @return A ReactiveJwtDecoder instance.
     */
    @Bean
    @Generated(reason = "Only called with a valid client registration repository")
    @ConditionalOnProperty("spring.security.oauth2.client.provider.oidc.issuer-uri")
    fun jwtDecoder(
        clientRegistrationRepository: ReactiveClientRegistrationRepository
    ): ReactiveJwtDecoder {
        val jwtDecoder = NimbusReactiveJwtDecoder.withIssuerLocation(issuerUri).build()
        val audienceValidator: OAuth2TokenValidator<Jwt> =
            AudienceValidator(applicationSecurityProperties.oauth2.audience)
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri)
        val withAudience: OAuth2TokenValidator<Jwt> =
            DelegatingOAuth2TokenValidator(withIssuer, audienceValidator)
        jwtDecoder.setJwtValidator(withAudience)

        return ReactiveJwtDecoder { token ->
            jwtDecoder.decode(token)
                .flatMap { jwt ->
                    clientRegistrationRepository.findByRegistrationId("oidc")
                        .flatMap {
                            val webClient = WebClient.builder()
                                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                .clientConnector(
                                    ReactorClientHttpConnector(
                                        HttpClient.create()
                                            .responseTimeout(Duration.ofMillis(TIMEOUT.toLong())),
                                    ),
                                )
                                .baseUrl(it!!.providerDetails.issuerUri)
                                .build()
                            jwtDecoder.setClaimSetConverter(
                                CustomClaimConverter(
                                    it,
                                    webClient,
                                    token,
                                ),
                            )
                            Mono.just(jwt)
                        }
                }
        }
    }

    @Bean
    fun strictTransportSecurityWebFilter(): WebFilter {
        if (!applicationSecurityProperties.headers.hstsEnabled) {
            return WebFilter { exchange, chain -> chain.filter(exchange) }
        }

        val directives = buildString {
            append("max-age=")
            append(applicationSecurityProperties.headers.hstsMaxAge)
            if (applicationSecurityProperties.headers.hstsIncludeSubdomains) {
                append("; includeSubDomains")
            }
            if (applicationSecurityProperties.headers.hstsPreload) {
                append("; preload")
            }
        }

        return WebFilter { exchange, chain ->
            val forwardedProto = exchange.request.headers.getFirst(X_FORWARDED_PROTO)?.lowercase()
            val secure = exchange.request.sslInfo != null || forwardedProto == "https"
            if (secure) {
                exchange.response.headers.set(STRICT_TRANSPORT_SECURITY_HEADER, directives)
            }
            chain.filter(exchange)
        }
    }

    companion object {
        private const val TIMEOUT = 2000
        private const val STRICT_TRANSPORT_SECURITY_HEADER = "Strict-Transport-Security"
        private const val X_FORWARDED_PROTO = "X-Forwarded-Proto"
    }
}
