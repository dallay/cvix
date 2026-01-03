// Note: SSL_CERT_PATH and SSL_KEY_PATH are intentionally not exported from here
// to avoid dragging Node.js built-ins (node:path, node:url) into browser bundles.
// If you need them in a Node environment (e.g. Astro/Vite config),
// import them directly from '@cvix/lib/src/consts/ssl-paths'.

export {
	BACKEND_PORT,
	BASE_API_URL,
	// Deprecated (backward compatibility - remove in v2.0)
	BASE_DOCS_URL,
	BASE_URL,
	BASE_WEBAPP_URL,
	BLOG_PORT,
	BLOG_URL,
	// Brand constants (unchanged)
	BRAND_NAME,
	CVIX_API_URL,
	CVIX_BLOG_URL,
	CVIX_DOCS_URL,
	// New semantic naming (preferred)
	CVIX_MARKETING_URL,
	CVIX_OAUTH_URL,
	CVIX_WEBAPP_URL,
	DOCS_PORT,
	LANDING_PAGE_PORT,
	PORTS,
	SITE_TITLE,
	WEBAPP_PORT,
	X_ACCOUNT,
} from "./config.ts";
