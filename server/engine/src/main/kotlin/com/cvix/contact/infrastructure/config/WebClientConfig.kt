package com.cvix.contact.infrastructure.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import java.time.Duration
import java.util.concurrent.TimeUnit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

/**
 * WebClient configuration for reactive HTTP clients.
 *
 * Provides configured WebClient beans with:
 * - Connection timeout (5 seconds)
 * - Read timeout (10 seconds)
 * - Write timeout (10 seconds)
 * - Connection pool with max idle time
 *
 * Includes a specialized WebClient for hCaptcha verification with optimized timeouts.
 */
@Configuration
class WebClientConfig {

    /**
     * General-purpose WebClient for reactive HTTP calls.
     */
    @Bean
    fun webClient(): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS)
            .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                conn.addHandlerLast(WriteTimeoutHandler(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    /**
     * Specialized WebClient for hCaptcha verification API.
     *
     * Optimized with shorter timeouts suitable for captcha verification:
     * - Connection timeout: 3 seconds
     * - Read/Response timeout: 5 seconds
     * - Base URL pre-configured for hCaptcha API
     * - Default Content-Type header
     */
    @Bean
    fun hcaptchaWebClient(): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, HCAPTCHA_CONNECTION_TIMEOUT_MILLIS)
            .responseTimeout(Duration.ofSeconds(HCAPTCHA_RESPONSE_TIMEOUT_SECONDS))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(HCAPTCHA_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                conn.addHandlerLast(WriteTimeoutHandler(HCAPTCHA_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            }

        return WebClient.builder()
            .baseUrl("https://hcaptcha.com")
            .defaultHeader("Content-Type", "application/x-www-form-urlencoded")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    companion object {
        // General WebClient timeouts
        private const val CONNECTION_TIMEOUT_MILLIS = 5000
        private const val RESPONSE_TIMEOUT_SECONDS = 10L
        private const val READ_TIMEOUT_SECONDS = 10L
        private const val WRITE_TIMEOUT_SECONDS = 10L

        // hCaptcha-specific optimized timeouts
        private const val HCAPTCHA_CONNECTION_TIMEOUT_MILLIS = 3000
        private const val HCAPTCHA_RESPONSE_TIMEOUT_SECONDS = 5L
        private const val HCAPTCHA_READ_TIMEOUT_SECONDS = 5L
        private const val HCAPTCHA_WRITE_TIMEOUT_SECONDS = 5L
    }
}
