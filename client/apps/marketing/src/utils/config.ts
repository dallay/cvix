/**
 * Utility to get the site URL for Astro configuration
 * Uses CF_PAGES_URL environment variable when available (Cloudflare Pages)
 * Falls back to production URL for local development
 */
export function getSiteUrl(): string {
	return process.env.CF_PAGES_URL || "https://example.com";
}
