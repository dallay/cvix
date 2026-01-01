// Note: SSL_CERT_PATH and SSL_KEY_PATH are intentionally not exported from here
// to avoid dragging Node.js built-ins (node:path, node:url) into browser bundles.
// If you need them in a Node environment (e.g. Astro/Vite config),
// import them directly from '@cvix/lib/src/consts/ssl-paths'.

export {
	BASE_DOCS_URL,
	BASE_URL,
	BASE_WEBAPP_URL,
	BLOG_PORT,
	BLOG_URL,
	BRAND_NAME,
	DOCS_PORT,
	LANDING_PAGE_PORT,
	SITE_TITLE,
	WEBAPP_PORT,
	X_ACCOUNT,
} from "./config";
