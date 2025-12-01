package com.cvix.resume.infrastructure.pdf

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import java.time.Duration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private const val MAX_CONNECTIONS = 100

/**
 * Docker configuration for PDF generation.
 * Configures Docker client and resource limits per security requirements.
 */
@Configuration
@EnableConfigurationProperties(DockerPdfGeneratorProperties::class)
class DockerConfiguration {

    @Bean
    fun dockerClient(properties: DockerPdfGeneratorProperties): DockerClient {
        val config = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build()

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(config.dockerHost)
            .sslConfig(config.sslConfig)
            .maxConnections(MAX_CONNECTIONS)
            .connectionTimeout(Duration.ofSeconds(properties.timeoutSeconds))
            .responseTimeout(Duration.ofSeconds(properties.timeoutSeconds))
            .build()

        return DockerClientImpl.getInstance(config, httpClient)
    }
}
