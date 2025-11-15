package com.loomify.resume.infrastructure.template.renders

import com.loomify.resume.infrastructure.template.util.LatexEscaper
import java.util.*
import org.stringtemplate.v4.AttributeRenderer

/**
 * Custom renderer for URLs that can shorten them to a more readable format.
 *
 * Supports format strings:
 * - "short" or "shortened": Removes protocol (https://, http://) and displays domain/path
 * - "latex": Escapes LaTeX special characters for safe rendering
 * - "short-latex": Shortened URL with LaTeX escaping
 * - null or empty: Returns full URL as-is
 *
 * Examples:
 * - <url; format="short"> for "https://github.com/yacosta738/flutter_shop_app"
 *   renders as "github.com/yacosta738/flutter_shop_app"
 * - <url; format="short-latex"> renders as "github.com/yacosta738/flutter\_shop\_app"
 * - <url> renders as "https://github.com/yacosta738/flutter_shop_app"
 */
class UrlRenderer : AttributeRenderer<String> {
    override fun toString(url: String?, formatString: String?, locale: Locale?): String {
        if (url == null) return ""

        return when (formatString?.lowercase()) {
            "short", "shortened" -> shortenUrl(url)
            "latex" -> escapeLatex(url)
            "short-latex", "shortened-latex" -> escapeLatex(shortenUrl(url))
            else -> url
        }
    }

    /**
     * Removes the protocol (https://, http://, www.) from the URL
     */
    private fun shortenUrl(url: String): String {
        return url
            .replace(Regex("^https?://"), "") // Remove http:// or https://
            .replace(Regex("^www\\."), "") // Remove www. if present
            .trimEnd('/') // Remove trailing slash if present
    }

    /**
     * Escapes LaTeX special characters in the URL
     */
    private fun escapeLatex(text: String): String {
        return LatexEscaper.escape(text)
    }
}
