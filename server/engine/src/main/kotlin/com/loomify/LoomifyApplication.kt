package com.loomify

import com.loomify.ApplicationStartupTraces.initApplication
import com.loomify.common.domain.Service
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@ComponentScan(
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [Service::class],
        ),
    ],
)
class LoomifyApplication

private val log: Logger = LoggerFactory.getLogger(LoomifyApplication::class.java)

fun main(args: Array<String>) {
    val environment = runApplication<LoomifyApplication>(args = args).environment
    initApplication(environment)

    if (log.isInfoEnabled) {
        log.info(ApplicationStartupTraces.of(environment))
    }
}
