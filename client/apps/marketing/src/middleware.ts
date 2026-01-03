/**
 * Middleware for handling i18n redirects and 404 pages
 *
 * This middleware ensures:
 * 1. Root path "/" redirects to default locale "/en/"
 * 2. Paths without locale prefix redirect to "/en/{path}"
 * 3. Preserves query parameters and trailing slashes
 */

import { sequence } from "astro:middleware";
import { DEFAULT_LOCALE } from "@cvix/i18n";
import type { APIContext, MiddlewareNext } from "astro";

const SUPPORTED_LOCALES = ["en", "es"] as const;

/**
 * Middleware to redirect non-localized paths to the default locale
 */
async function redirectToDefaultLocale(
	context: APIContext,
	next: MiddlewareNext,
) {
	const { pathname, search } = context.url;

	// Skip static assets and API routes
	if (
		pathname.startsWith("/_astro/") ||
		pathname.startsWith("/admin") ||
		pathname.endsWith(".txt") ||
		pathname.endsWith(".xml") ||
		pathname.endsWith(".json") ||
		pathname.endsWith(".ico") ||
		pathname.endsWith(".png") ||
		pathname.endsWith(".svg") ||
		pathname.endsWith(".jpg") ||
		pathname.endsWith(".webp") ||
		pathname.endsWith(".avif") ||
		pathname.startsWith("/cdn-cgi/")
	) {
		return next();
	}

	// Check if path already has a locale prefix
	const pathSegments = pathname.split("/").filter(Boolean);
	const firstSegment = pathSegments[0];
	const hasLocalePrefix = SUPPORTED_LOCALES.includes(
		firstSegment as (typeof SUPPORTED_LOCALES)[number],
	);

	// If already has locale prefix, continue
	if (hasLocalePrefix) {
		return next();
	}

	// Redirect root to default locale
	if (pathname === "/") {
		return context.redirect(`/${DEFAULT_LOCALE}/`, 301);
	}

	// Redirect paths without locale to default locale
	const newPath = `/${DEFAULT_LOCALE}${pathname}${search}`;
	return context.redirect(newPath, 301);
}

/**
 * Export the middleware sequence
 */
export const onRequest = sequence(redirectToDefaultLocale);
