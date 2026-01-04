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

// ============================================================================
// ENVIRONMENT VARIABLE ACCESS
// ============================================================================

/**
 * Access environment variables across Node.js, Vite, and Astro
 * Checks multiple sources in order of precedence
 */
export const getEnv = (key: string): string | undefined => {
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
};

// ============================================================================
// URL RESOLUTION FUNCTIONS
// ============================================================================

/**
 * Resolve URL with intelligent defaults based on provider and environment
 */
function resolveUrl(config: {
	envKey: string; // Environment variable name (CVIX_MARKETING_URL)
	providerDefaults?: {
		// Provider-specific auto-detection
		cloudflare?: string; // CF_PAGES_URL
		vercel?: string; // VERCEL_URL
	};
	genericDefault?: string; // Generic convention (SITE_URL)
	localPort?: number; // Localhost port for development
	protocol?: "http" | "https"; // Protocol for localhost (default: http)
}): string {
	const { envKey, providerDefaults, genericDefault, localPort, protocol } =
		config;

	// 1. Explicit environment variable (highest priority)
	const explicitUrl = getEnv(envKey);
	if (explicitUrl) return explicitUrl;

	// 2. Provider-specific auto-detection
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
				if (vercelUrl) return `https://${vercelUrl}`;
				break;
			}
		}
	}

	// 3. Generic convention
	if (genericDefault) {
		const genericUrl = getEnv(genericDefault);
		if (genericUrl) return genericUrl;
	}

	// 4. Localhost development fallback
	if (localPort) {
		const proto = protocol ?? "http";
		return `${proto}://localhost:${localPort}`;
	}

	// 5. Last resort: empty string (should never happen)
	// Only warn in development to avoid polluting production logs
	if (import.meta.env?.DEV) {
		console.warn(`⚠️  No URL found for ${envKey}`);
	}
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
 * - Explicit: CVIX_MARKETING_URL
 * - Cloudflare: CF_PAGES_URL (auto-detected)
 * - Fallback: http://localhost:7766
 */
export const CVIX_MARKETING_URL = resolveUrl({
	envKey: "CVIX_MARKETING_URL",
	providerDefaults: { cloudflare: "CF_PAGES_URL" },
	genericDefault: "SITE_URL",
	localPort: PORTS.MARKETING,
});

/**
 * Main web application URL (Vue.js SPA)
 * - Explicit: CVIX_WEBAPP_URL
 * - Fallback: http://localhost:9876
 */
export const CVIX_WEBAPP_URL = resolveUrl({
	envKey: "CVIX_WEBAPP_URL",
	localPort: PORTS.WEBAPP,
});

/**
 * Documentation site URL
 * - Explicit: CVIX_DOCS_URL
 * - Fallback: http://localhost:4321
 */
export const CVIX_DOCS_URL = resolveUrl({
	envKey: "CVIX_DOCS_URL",
	localPort: PORTS.DOCS,
});

/**
 * Blog site URL
 * - Explicit: CVIX_BLOG_URL
 * - Fallback: http://localhost:7767
 */
export const CVIX_BLOG_URL = resolveUrl({
	envKey: "CVIX_BLOG_URL",
	localPort: PORTS.BLOG,
});

/**
 * Backend REST API URL
 * - Explicit: CVIX_API_URL
 * - Fallback: https://localhost:8443 (HTTPS for OAuth)
 */
export const CVIX_API_URL = resolveUrl({
	envKey: "CVIX_API_URL",
	localPort: PORTS.API,
	protocol: "https", // OAuth requires HTTPS
});

/**
 * OAuth/Keycloak server URL
 * - Production: CVIX_OAUTH_URL or OAUTH2_SERVER_URL (legacy)
 * - Local: http://localhost:9080
 */
export const CVIX_OAUTH_URL = resolveUrl({
	envKey: "CVIX_OAUTH_URL",
	genericDefault: "OAUTH2_SERVER_URL", // Legacy fallback
	localPort: 9080,
});

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
