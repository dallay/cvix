package com.cvix.config.infrastructure.http

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.servers.Server
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "Cvix API",
        version = "1.0",
        description = "REST API for Cvix platform - Resume/CV management and automation",
        contact = Contact(
            name = "Cvix Support",
            email = "support@cvix.com",
            url = "https://cvix.com",
        ),
    ),
    servers = [
        Server(url = "/", description = "Default Server URL"),
    ],
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT Authorization header using the Bearer scheme.",
)
class OpenApiConfiguration
