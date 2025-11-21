package com.loomify.engine

import com.loomify.IntegrationTest
import com.loomify.spring.boot.bus.event.EventConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@TestPropertySource(properties = ["spring.test.context.testcontainers.enabled=false"])
internal class LoomifyApplicationTests {
    @Suppress("UnusedPrivateProperty")
    @Autowired
    private lateinit var eventConfiguration: EventConfiguration
    @Test
    fun should_load_context() = Unit
}
