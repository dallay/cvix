// Note: SSL_CERT_PATH and SSL_KEY_PATH are intentionally not exported from here
// to avoid dragging Node.js built-ins (node:path, node:url) into browser bundles.
// If you need them in a Node environment (e.g. Astro/Vite config),
// import them directly from '@cvix/lib/src/consts/ssl-paths'.

export {
	BRAND_NAME,
	CVIX_API_URL,
	CVIX_BLOG_URL,
	CVIX_DOCS_URL,
	CVIX_MARKETING_URL,
	CVIX_OAUTH_URL,
	CVIX_WEBAPP_URL,
	PORTS,
	SITE_TITLE,
	X_ACCOUNT,
} from "./config";
