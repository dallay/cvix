/**
 * Shared Astro configuration utilities for CVIX apps.
 *
 * This module provides reusable configuration helpers to reduce duplication
 * across multiple Astro applications in the monorepo.
 *
 * @example
 * ```ts
 * import {
 *   getHttpsConfig,
 *   CVIX_SSR_NO_EXTERNAL,
 *   DEFAULT_ICON_CONFIG,
 *   createI18nConfig,
 *   createSitemapConfig,
 *   DEFAULT_IMAGE_CONFIG,
 * } from "@cvix/lib/astro";
 * ```
 */

// i18n configuration
export { createI18nConfig, DEFAULT_I18N_ROUTING } from "./i18n-config.js";
export type { ImageConfig } from "./image-config.js";
// Image service configuration
export { DEFAULT_IMAGE_CONFIG } from "./image-config.js";
export type { IconConfig, SitemapConfigOptions } from "./integrations.js";

// Integration configurations
export {
	createSitemapConfig,
	DEFAULT_ICON_CONFIG,
} from "./integrations.js";
export type { HttpsConfig } from "./ssl-config.js";
// SSL/HTTPS configuration
export { getHttpsConfig, hasSSLCertificates } from "./ssl-config.js";
export type { CvixSsrNoExternal } from "./vite-config.js";
// Vite SSR configuration
export { CVIX_SSR_NO_EXTERNAL } from "./vite-config.js";
