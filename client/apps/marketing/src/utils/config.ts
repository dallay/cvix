/**
 * Normalize and validate a site URL
 * @internal
 * @param url - The URL to normalize
 * @param fallback - Fallback URL if the input is invalid
 * @returns A valid, normalized URL without trailing slashes
 */
function normalizeSiteUrl(url: string | undefined, fallback: string): string {
  if (!url) return fallback;
  const trimmed = url.trim();
  if (trimmed && /^https?:\/\/.+/.test(trimmed)) {
    return trimmed.replace(/\/+$/, "");
  }
  return fallback;
}

// Memoize the site URL to avoid repeated environment variable lookups
let cachedSiteUrl: string | undefined;

/**
 * Reset the memoized site URL cache (for testing purposes only)
 * @internal
 */
export function __resetSiteUrlCache(): void {
  cachedSiteUrl = undefined;
}

/**
 * Utility to get the site URL for Astro configuration (server-side only)
 *
 * @remarks
 * This function uses Node.js process.env and is intended for server-side/SSR use only (e.g., astro.config.ts or build-time code).
 * Do NOT call this from browser/client bundles; process.env is not available in client-side code.
 * For client-side URL access, use {@link getBaseUrl}, {@link getDocsUrl}, or {@link getWebappUrl} which use import.meta.env.
 *
 * Environment variable precedence:
 *   1. Tries SITE_URL first
 *   2. Then CF_PAGES_URL
 *   3. Otherwise uses the default production URL ("https://cvix.pages.dev")
 * All values are normalized (trimmed, validated, trailing slashes removed). If normalization fails, the default production URL is returned.
 * @returns A valid, normalized site URL
 */
export function getSiteUrl(): string {
  if (cachedSiteUrl) {
    return cachedSiteUrl;
  }

  const productionUrl = "https://cvix.pages.dev";
  const candidates: (string | undefined)[] = [
    process.env.SITE_URL,
    process.env.CF_PAGES_URL,
  ];

  for (const url of candidates) {
    if (url) {
      const normalized = normalizeSiteUrl(url, productionUrl);
      if (normalized !== productionUrl) {
        cachedSiteUrl = normalized;
        return cachedSiteUrl;
      }
    }
  }

  cachedSiteUrl = productionUrl;
  return cachedSiteUrl;
}

/**
 * Get the base URL for the marketing/landing site
 * Uses environment variables with fallback to defaults
 * @returns Marketing site URL
 */
export function getBaseUrl(): string {
  const isDev = import.meta.env.DEV;
  const defaultLocal = "http://localhost:4321";

  if (isDev) {
    return import.meta.env.PUBLIC_BASE_URL_LOCAL || defaultLocal;
  }

  const explicitProd = import.meta.env.PUBLIC_BASE_URL_PROD;
  const normalizedProd = normalizeSiteUrl(
    explicitProd,
    "https://cvix.pages.dev"
  );
  return normalizedProd;
}

/**
 * Get the base URL for the documentation site
 * Uses environment variables with fallback to defaults
 * @returns Documentation site URL (subdomain or path-based)
 */
export function getDocsUrl(): string {
  const isDev = import.meta.env.DEV;
  const defaultLocal = "http://localhost:4321";

  if (isDev) {
    return import.meta.env.PUBLIC_BASE_DOCS_URL_LOCAL || defaultLocal;
  }

  // In production, prefer explicit env var or derive from site URL
  const explicitUrl = import.meta.env.PUBLIC_BASE_DOCS_URL_PROD;
  if (explicitUrl) {
    return explicitUrl;
  }

  // Fallback: append /docs to the normalized site URL
  const site = normalizeSiteUrl(
    import.meta.env.PUBLIC_SITE_URL,
    "https://cvix.pages.dev"
  );
  return `${site}/docs`;
}

/**
 * Get the base URL for the web application
 * Uses environment variables with fallback to defaults
 * @returns Web application URL
 */
export function getWebappUrl(): string {
  const isDev = import.meta.env.DEV;
  const defaultLocal = "http://localhost:9876";

  if (isDev) {
    return import.meta.env.PUBLIC_BASE_WEBAPP_URL_LOCAL || defaultLocal;
  }

  const explicitProd = import.meta.env.PUBLIC_BASE_WEBAPP_URL_PROD;
  const normalizedProd = normalizeSiteUrl(
    explicitProd,
    "https://app.cvix.pages.dev"
  );
  return normalizedProd;
}
