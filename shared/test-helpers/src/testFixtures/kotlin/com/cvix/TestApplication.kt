package com.cvix

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
@EnableAutoConfiguration
@ConfigurationPropertiesScan
open class TestApplication
