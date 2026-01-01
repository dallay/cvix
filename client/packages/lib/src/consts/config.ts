/**
 * The brand name of the application, used for display and metadata.
 * @type {string}
 */
export const BRAND_NAME: string = "ProFileTailors";

/**
 * The site title, used for document titles and SEO.
 * @type {string}
 */
export const SITE_TITLE: string = "ProFileTailors";

/**
 * The X (Twitter) account handle for the brand.
 * @type {string}
 */
export const X_ACCOUNT: string = "@yacosta738";

// Application Constants
/**
 * Port for the main web application (Vue SPA).
 * @type {number}
 */
export const WEBAPP_PORT: number = 9876;
/**
 * Port for the landing/marketing page (Astro).
 * @type {number}
 */
export const LANDING_PAGE_PORT: number = 7766;
/**
 * Port for documentation site.
 * @type {number}
 */
export const DOCS_PORT: number = 4321;
/**
 * Port for the blog site.
 * @type {number}
 */
export const BLOG_PORT: number = 7767;
/**
 * Port for the backend API server.
 * @type {number}
 */
export const BACKEND_PORT: number = 8443;

// Base URLs - Using centralized config functions
/**
 * HTTP protocol string.
 * @type {string}
 * @private
 */
// Protocols
const HTTP = "http://";
const HTTPS = "https://";

/**
 * Safely access environment variables in both Node.js and browser environments.
 * @param key The name of the environment variable.
 * @returns The value of the environment variable, or undefined.
 */
const getEnv = (key: string): string | undefined => {
	// Try process.env (Node.js)
	if (typeof process !== "undefined" && process.env) {
		return process.env[key];
	}

	// Try import.meta.env (Vite/Astro)
	if (typeof import.meta !== "undefined" && import.meta.env) {
		return (
			import.meta.env[key] ||
			import.meta.env[`PUBLIC_${key}`] ||
			import.meta.env[`VITE_${key}`]
		);
	}

	return undefined;
};

/**
 * The base URL for the landing page, resolved from environment variables or defaults to localhost.
 * @type {string}
 */
export const BASE_URL =
	getEnv("BASE_URL") ||
	getEnv("SITE_URL") ||
	getEnv("CF_PAGES_URL") ||
	`${HTTP}localhost:${LANDING_PAGE_PORT}`;

/**
 * The base URL for documentation site, resolved from environment variables or defaults to localhost.
 * @type {string}
 */
export const BASE_DOCS_URL =
	getEnv("BASE_DOCS_URL") ||
	getEnv("CF_PAGES_URL") ||
	`${HTTP}localhost:${DOCS_PORT}`;

/**
 * The base URL for the web application, resolved from environment variables or defaults to localhost.
 * @type {string}
 */
export const BASE_WEBAPP_URL =
	getEnv("BASE_WEBAPP_URL") ||
	getEnv("CF_PAGES_URL") ||
	`${HTTP}localhost:${WEBAPP_PORT}`;

/**
 * The base URL for the blog site, resolved from environment variables or defaults to localhost.
 * @type {string}
 */
export const BLOG_URL =
	getEnv("BLOG_URL") ||
	getEnv("CF_PAGES_URL") ||
	`${HTTP}localhost:${BLOG_PORT}`;

/**
 * The base URL for the backend API, resolved from environment variables or defaults to localhost (HTTPS).
 * @type {string}
 */
export const BASE_API_URL =
	getEnv("BASE_API_URL") || `${HTTPS}localhost:${BACKEND_PORT}`;
