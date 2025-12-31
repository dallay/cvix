export const BRAND_NAME: string = "ProFileTailors";
export const SITE_TITLE: string = "ProFileTailors";

export const X_ACCOUNT: string = "@yacosta738";

// Application Constants
export const WEBAPP_PORT: number = 9876;
export const LANDING_PAGE_PORT: number = 7766;
export const DOCS_PORT: number = 4321;
export const BLOG_PORT: number = 7767;
export const BACKEND_PORT: number = 8443;

// Base URLs - Using centralized config functions
const HTTP = "http://";
const HTTPS = "https://";
export const BASE_URL =
	process.env.BASE_URL ||
	process.env.SITE_URL ||
	process.env.CF_PAGES_URL ||
	`${HTTP}localhost:${LANDING_PAGE_PORT}`;
export const BASE_DOCS_URL =
	process.env.BASE_DOCS_URL ||
	process.env.CF_PAGES_URL ||
	`${HTTP}localhost:${DOCS_PORT}`;
export const BASE_WEBAPP_URL =
	process.env.BASE_WEBAPP_URL ||
	process.env.CF_PAGES_URL ||
	`${HTTP}localhost:${WEBAPP_PORT}`;
export const BLOG_URL =
	process.env.BLOG_URL ||
	process.env.CF_PAGES_URL ||
	`${HTTP}localhost:${BLOG_PORT}`;
export const BASE_API_URL =
	process.env.BASE_API_URL || `${HTTPS}localhost:${BACKEND_PORT}`;
