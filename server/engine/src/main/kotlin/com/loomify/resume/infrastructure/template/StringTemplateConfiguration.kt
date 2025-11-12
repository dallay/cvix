package com.loomify.resume.infrastructure.template

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.stringtemplate.v4.STGroupString

/**
 * StringTemplate 4 configuration for LaTeX template rendering.
 * Templates are loaded as raw strings to avoid ST4 parsing LaTeX syntax.
 * 
 * Uses @ as delimiters instead of $ to avoid conflicts with LaTeX commands.
 */
@Configuration
class StringTemplateConfiguration {

    @Bean
    fun resumeTemplateGroup(): STGroupString {
        // We use STGroupString to load templates as raw content
        // This prevents ST4 from trying to parse LaTeX commands as ST4 syntax
        // Using @ delimiters to avoid conflicts with $ in LaTeX math mode
        return STGroupString("template-group", "", '@', '@')
    }
}
