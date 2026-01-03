/**
 * Middleware for handling i18n redirects and 404 pages
 *
 * This middleware ensures:
 * 1. Root path "/" redirects to default locale "/en/"
 * 2. Paths without locale prefix redirect to "/en/{path}"
 * 3. Preserves query parameters and trailing slashes
 */

import { defineMiddleware, sequence } from "astro:middleware";
import { DEFAULT_LOCALE, isSupportedLocale } from "@cvix/i18n";

/**
 * Static file extensions to skip i18n processing
 */
const STATIC_EXTENSIONS = new Set([
	".txt",
	".xml",
	".json",
	".ico",
	".png",
	".svg",
	".jpg",
	".webp",
	".avif",
]);

/**
 * Path prefixes to skip i18n processing
 */
const SKIP_PATH_PREFIXES = ["/_astro/", "/admin", "/cdn-cgi/"];

/**
 * Middleware to redirect non-localized paths to the default locale
 */
export const redirectToDefaultLocale = defineMiddleware(
	async (context, next) => {
		const { pathname, search } = context.url;

		// Skip static assets and API routes
		const ext = pathname.substring(pathname.lastIndexOf("."));
		const isStaticAsset =
			SKIP_PATH_PREFIXES.some((prefix) => pathname.startsWith(prefix)) ||
			STATIC_EXTENSIONS.has(ext);

		if (isStaticAsset) {
			return next();
		}

		// Check if path already has a locale prefix
		const pathSegments = pathname.split("/").filter(Boolean);
		const firstSegment = pathSegments[0];
		const hasLocalePrefix = isSupportedLocale(firstSegment);

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
	},
);

/**
 * Export the middleware sequence
 */
export const onRequest = sequence(redirectToDefaultLocale);
