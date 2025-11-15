package com.loomify.resume.infrastructure.template.util

/**
 * Utility to escape LaTeX special characters in user-provided content.
 * Escapes: \ & % $ # _ { } ~ ^ < >
 * Always escape backslash first to avoid double escaping newly inserted commands.
 */
object LatexEscaper {
    fun escape(input: String): String = input
        .replace("\\", "\\textbackslash{}")
        .replace("&", "\\&")
        .replace("%", "\\%")
        .replace("$", "\\$")
        .replace("#", "\\#")
        .replace("_", "\\_")
        .replace("{", "\\{")
        .replace("}", "\\}")
        .replace("<", "\\textless{}")
        .replace(">", "\\textgreater{}")
        .replace("~", "\\textasciitilde{}")
        .replace("^", "\\textasciicircum{}")
}
