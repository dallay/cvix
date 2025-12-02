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

/**
 * Get the base URL for the marketing/landing site
 * Uses environment variables with fallback to defaults
 */
export function getBaseUrl(): string {
  const isDev = import.meta.env.DEV;
  const defaultLocal = "http://localhost:4321";
  const defaultProd = getSiteUrl();

  if (isDev) {
    return process.env.BASE_URL_LOCAL || defaultLocal;
  }
  return process.env.BASE_URL_PROD || defaultProd;
}

/**
 * Get the base URL for the documentation site
 * Uses environment variables with fallback to defaults
 */
export function getDocsUrl(): string {
  const isDev = import.meta.env.DEV;
  const defaultLocal = "http://localhost:4321";
  const defaultProd = `https://docs.${getSiteUrl().replace(/^https?:\/\//, "")}`;

  if (isDev) {
    return process.env.BASE_DOCS_URL_LOCAL || defaultLocal;
  }
  return process.env.BASE_DOCS_URL_PROD || defaultProd;
}

/**
 * Get the base URL for the web application
 * Uses environment variables with fallback to defaults
 */
export function getWebappUrl(): string {
  const isDev = import.meta.env.DEV;
  const defaultLocal = "http://localhost:9876";
  const defaultProd = "https://app.cvix.pages.dev";

  if (isDev) {
    return process.env.BASE_WEBAPP_URL_LOCAL || defaultLocal;
  }
  return process.env.BASE_WEBAPP_URL_PROD || defaultProd;
}
