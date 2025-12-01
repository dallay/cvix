/**
 * Utility to get the site URL for Astro configuration
 * Uses CF_PAGES_URL environment variable when available (Cloudflare Pages)
 * Falls back to production URL for local development
 */
export function getSiteUrl(): string {
  const productionUrl = "https://cvix.pages.dev";
  const candidates = [
    process.env.SITE_URL,
    process.env.CF_PAGES_URL,
    productionUrl
  ];
  for (const url of candidates) {
    if (url && url.trim() !== "") {
      return url;
    }
  }
  return productionUrl;
}
