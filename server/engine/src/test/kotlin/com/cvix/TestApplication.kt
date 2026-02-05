package com.cvix

import org.springframework.boot.builder.SpringApplicationBuilder

fun main(args: Array<String>) {
    SpringApplicationBuilder(Application::class.java)
        .sources(TestcontainersConfiguration::class.java)
        .profiles("local")
        .run(*args)
}
