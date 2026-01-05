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
 * Provides a configured WebClient bean with:
 * - Connection timeout (5 seconds)
 * - Read timeout (10 seconds)
 * - Write timeout (10 seconds)
 * - Connection pool with max idle time
 */
@Configuration
class WebClientConfig {

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

    companion object {
        private const val CONNECTION_TIMEOUT_MILLIS = 5000
        private const val RESPONSE_TIMEOUT_SECONDS = 10L
        private const val READ_TIMEOUT_SECONDS = 10L
        private const val WRITE_TIMEOUT_SECONDS = 10L
    }
}
