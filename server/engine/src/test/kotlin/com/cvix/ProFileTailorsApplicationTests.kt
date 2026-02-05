package com.cvix

import com.cvix.config.InfrastructureTestContainers
import com.cvix.spring.boot.bus.event.EventConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@IntegrationTest
internal class ProFileTailorsApplicationTests : InfrastructureTestContainers() {
    @Suppress("UnusedPrivateProperty")
    @Autowired
    private lateinit var eventConfiguration: EventConfiguration

    @Test
    fun should_load_context() = Unit
}
