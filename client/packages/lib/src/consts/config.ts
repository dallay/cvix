// ============================================================================
// ENVIRONMENT DETECTION
// ============================================================================

/**
 * Detect deployment provider from environment
 */
type DeploymentProvider = "cloudflare" | "vercel" | "netlify" | "local";

function detectProvider(): DeploymentProvider {
	if (typeof process !== "undefined" && process.env) {
		if (process.env.CF_PAGES) return "cloudflare";
		if (process.env.VERCEL) return "vercel";
		if (process.env.NETLIFY) return "netlify";
	}
	return "local";
}

const DEPLOYMENT_PROVIDER = detectProvider();
const IS_PRODUCTION = getEnv("NODE_ENV") === "production";

// ============================================================================
// ENVIRONMENT VARIABLE ACCESS
// ============================================================================

/**
 * Access environment variables across Node.js, Vite, and Astro
 * Checks multiple sources in order of precedence
 */
function getEnv(key: string): string | undefined {
	// Node.js environment
	if (typeof process !== "undefined" && process.env?.[key]) {
		return process.env[key];
	}

	// Vite/Astro import.meta.env
	if (typeof import.meta !== "undefined" && import.meta.env) {
		// Direct key
		if (import.meta.env[key]) return import.meta.env[key];

		// Astro public prefix (PUBLIC_*)
		const publicKey = `PUBLIC_${key}`;
		if (import.meta.env[publicKey]) return import.meta.env[publicKey];

		// Legacy Vite prefix (VITE_*)
		const viteKey = `VITE_${key}`;
		if (import.meta.env[viteKey]) return import.meta.env[viteKey];
	}

	return undefined;
}

// ============================================================================
// URL RESOLUTION FUNCTIONS
// ============================================================================

/**
 * Resolve URL with intelligent defaults based on provider and environment
 */
function resolveUrl(config: {
	envKey: string; // Primary env var name (CVIX_MARKETING_URL)
	prodEnvKey?: string; // Production override (CVIX_MARKETING_URL_PROD)
	providerDefaults?: {
		// Provider-specific auto-detection
		cloudflare?: string; // CF_PAGES_URL
		vercel?: string; // VERCEL_URL
	};
	genericDefault?: string; // Generic convention (SITE_URL)
	localPort?: number; // Localhost port for development
}): string {
	const { envKey, prodEnvKey, providerDefaults, genericDefault, localPort } =
		config;

	// 1. Production override (highest priority in production)
	if (IS_PRODUCTION && prodEnvKey) {
		const prodUrl = getEnv(prodEnvKey);
		if (prodUrl) return prodUrl;
	}

	// 2. Explicit environment variable
	const explicitUrl = getEnv(envKey);
	if (explicitUrl) return explicitUrl;

	// 3. Provider-specific auto-detection
	if (providerDefaults) {
		switch (DEPLOYMENT_PROVIDER) {
			case "cloudflare": {
				const cfUrl = providerDefaults.cloudflare
					? getEnv(providerDefaults.cloudflare)
					: getEnv("CF_PAGES_URL");
				if (cfUrl) return cfUrl;
				break;
			}

			case "vercel": {
				const vercelUrl = providerDefaults.vercel
					? getEnv(providerDefaults.vercel)
					: getEnv("VERCEL_URL");
				if (vercelUrl) return vercelUrl ? `https://${vercelUrl}` : "";
				break;
			}
		}
	}

	// 4. Generic convention
	if (genericDefault) {
		const genericUrl = getEnv(genericDefault);
		if (genericUrl) return genericUrl;
	}

	// 5. Localhost development fallback
	if (localPort) {
		return `http://localhost:${localPort}`;
	}

	// 6. Last resort: empty string (should never happen)
	console.warn(`⚠️  No URL found for ${envKey}`);
	return "";
}

// ============================================================================
// PORTS (Development)
// ============================================================================

export const PORTS = {
	MARKETING: 7766,
	WEBAPP: 9876,
	DOCS: 4321,
	BLOG: 7767,
	API: 8443,
} as const;

// ============================================================================
// APPLICATION URLs
// ============================================================================

/**
 * Marketing/Landing page URL
 * - Production: CVIX_MARKETING_URL_PROD or CVIX_MARKETING_URL
 * - Cloudflare: CF_PAGES_URL
 * - Local: http://localhost:7766
 */
export const CVIX_MARKETING_URL = resolveUrl({
	envKey: "CVIX_MARKETING_URL",
	prodEnvKey: "CVIX_MARKETING_URL_PROD",
	providerDefaults: { cloudflare: "CF_PAGES_URL" },
	genericDefault: "SITE_URL",
	localPort: PORTS.MARKETING,
});

/**
 * Main web application URL (Vue.js SPA)
 * - Production: CVIX_WEBAPP_URL_PROD or CVIX_WEBAPP_URL
 * - Local: http://localhost:9876
 */
export const CVIX_WEBAPP_URL = resolveUrl({
	envKey: "CVIX_WEBAPP_URL",
	prodEnvKey: "CVIX_WEBAPP_URL_PROD",
	localPort: PORTS.WEBAPP,
});

/**
 * Documentation site URL
 * - Production: CVIX_DOCS_URL_PROD or CVIX_DOCS_URL
 * - Local: http://localhost:4321
 */
export const CVIX_DOCS_URL = resolveUrl({
	envKey: "CVIX_DOCS_URL",
	prodEnvKey: "CVIX_DOCS_URL_PROD",
	localPort: PORTS.DOCS,
});

/**
 * Blog site URL
 * - Production: CVIX_BLOG_URL_PROD or CVIX_BLOG_URL
 * - Local: http://localhost:7767
 */
export const CVIX_BLOG_URL = resolveUrl({
	envKey: "CVIX_BLOG_URL",
	prodEnvKey: "CVIX_BLOG_URL_PROD",
	localPort: PORTS.BLOG,
});

/**
 * Backend REST API URL
 * - Production: CVIX_API_URL_PROD or CVIX_API_URL
 * - Local: https://localhost:8443 (HTTPS for OAuth)
 */
export const CVIX_API_URL =
	resolveUrl({
		envKey: "CVIX_API_URL",
		prodEnvKey: "CVIX_API_URL_PROD",
	}) || `https://localhost:${PORTS.API}`;

/**
 * OAuth/Keycloak server URL
 * - Production: CVIX_OAUTH_URL or OAUTH2_SERVER_URL
 * - Local: http://localhost:9080
 */
export const CVIX_OAUTH_URL =
	getEnv("CVIX_OAUTH_URL") ||
	getEnv("OAUTH2_SERVER_URL") ||
	"http://localhost:9080";

// ============================================================================
// BACKWARD COMPATIBILITY (Deprecated - Remove in v2.0)
// ============================================================================

/**
 * @deprecated Use CVIX_MARKETING_URL instead
 */
export const BASE_URL = CVIX_MARKETING_URL;

/**
 * @deprecated Use CVIX_WEBAPP_URL instead
 */
export const BASE_WEBAPP_URL = CVIX_WEBAPP_URL;

/**
 * @deprecated Use CVIX_API_URL instead
 */
export const BASE_API_URL = CVIX_API_URL;

/**
 * @deprecated Use CVIX_DOCS_URL instead
 */
export const BASE_DOCS_URL = CVIX_DOCS_URL;

/**
 * @deprecated Use CVIX_BLOG_URL instead
 */
export const BLOG_URL = CVIX_BLOG_URL;

/**
 * @deprecated Use PORTS.MARKETING instead
 */
export const LANDING_PAGE_PORT = PORTS.MARKETING;

/**
 * @deprecated Use PORTS.WEBAPP instead
 */
export const WEBAPP_PORT = PORTS.WEBAPP;

/**
 * @deprecated Use PORTS.DOCS instead
 */
export const DOCS_PORT = PORTS.DOCS;

/**
 * @deprecated Use PORTS.BLOG instead
 */
export const BLOG_PORT = PORTS.BLOG;

/**
 * @deprecated Use PORTS.API instead
 */
export const BACKEND_PORT = PORTS.API;

// ============================================================================
// BRAND CONSTANTS (unchanged)
// ============================================================================

/**
 * The brand name of the application, used for display and metadata.
 */
export const BRAND_NAME = "ProFileTailors";

/**
 * The site title, used for document titles and SEO.
 */
export const SITE_TITLE = "ProFileTailors";

/**
 * The X (Twitter) account handle for the brand.
 */
export const X_ACCOUNT = "@yacosta738";
