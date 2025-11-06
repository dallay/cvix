package com.loomify.resume.infrastructure.template

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.stringtemplate.v4.STGroupDir

/**
 * StringTemplate 4 configuration for LaTeX template rendering.
 * Templates are located in resources/templates/resume/
 */
@Configuration
class StringTemplateConfiguration {

    @Bean
    fun resumeTemplateGroup(): STGroupDir {
        // Load templates from classpath
        val templateGroup = STGroupDir("templates/resume", '$', '$')

        // Configure template group
        templateGroup.delimiterStartChar = '$'
        templateGroup.delimiterStopChar = '$'

        return templateGroup
    }
}
