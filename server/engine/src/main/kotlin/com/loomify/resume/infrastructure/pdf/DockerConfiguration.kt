package com.loomify.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import java.time.Duration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val MAX_CONNECTIONS = 100

private const val CONNECTION_TIMEOUT = 30

private const val RESPONSE_TIMEOUT = 15

/**
 * Docker configuration for PDF generation.
 * Configures Docker client and resource limits per security requirements.
 */
@Configuration
@EnableConfigurationProperties(DockerPdfGeneratorProperties::class)
class DockerConfiguration {

    @Bean
    fun dockerClient(): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(MAX_CONNECTIONS)
            .connectionTimeout(Duration.ofSeconds(CONNECTION_TIMEOUT.toLong()))
            .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT.toLong()))
            .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }
}
