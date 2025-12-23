import { existsSync } from "node:fs";
import { fileURLToPath, URL } from "node:url";
import { SSL_CERT_PATH, SSL_KEY_PATH } from "../../packages/ssl-paths/index.js";
import mdx from "@astrojs/mdx";
import sitemap from "@astrojs/sitemap";
import vue from "@astrojs/vue";
import tailwindcss from "@tailwindcss/vite";
import {defineConfig, envField} from "astro/config";
import icon from "astro-icon";
import { BASE_URL } from "./src/consts.ts";
import { DEFAULT_LOCALE_SETTING, LOCALES_SETTING } from "./src/i18n/locales";
import { getSiteUrl } from "./src/utils/config.ts";

import { remarkReadingTime } from "./src/utils/remark-reading-time.mjs";

/**
 * Check if SSL certificates exist for HTTPS development
 * @returns {boolean} true if both cert and key files exist
 */
function hasSSLCertificates() {
	const certPath = fileURLToPath(new URL(SSL_CERT_PATH, import.meta.url));
	const keyPath = fileURLToPath(new URL(SSL_KEY_PATH, import.meta.url));
	
	const certExists = existsSync(certPath);
	const keyExists = existsSync(keyPath);
	
	return certExists && keyExists;
}

/**
 * Get HTTPS configuration for Vite dev server
 * Falls back to HTTP if certificates are not available
 * @returns {object | undefined} HTTPS config or undefined for HTTP
 */
function getHttpsConfig() {
	// Check for explicit HTTP-only mode via environment variable
	if (process.env.FORCE_HTTP === "true") {
		console.log("ℹ️  FORCE_HTTP=true detected, running in HTTP mode");
		return undefined;
	}

	// Check if certificates exist
	if (!hasSSLCertificates()) {
		console.warn("⚠️  SSL certificates not found. Running in HTTP mode.");
		console.warn("   To enable HTTPS, generate certificates with:");
		console.warn("   → cd infra && ./generate-ssl-certificate.sh");
		console.warn("   → OR run: make ssl-cert");
		console.warn("   → See: client/HTTPS_DEVELOPMENT.md for details");
		return undefined;
	}

	// Certificates exist, use HTTPS
	console.log("✅ SSL certificates found, running in HTTPS mode");
	return {
		key: fileURLToPath(new URL(SSL_KEY_PATH, import.meta.url)),
		cert: fileURLToPath(new URL(SSL_CERT_PATH, import.meta.url)),
	};
}

// https://astro.build/config
export default defineConfig({
	server: {
		port: 7766,
	},

	// Configure Sharp image service explicitly
	image: {
		service: {
			entrypoint: 'astro/assets/services/sharp',
			config: {
				limitInputPixels: false,
			},
		},
	},
	// Set your site's URL
	site: getSiteUrl(),

	i18n: {
		defaultLocale: DEFAULT_LOCALE_SETTING,
		locales: Object.keys(LOCALES_SETTING),
		routing: {
			prefixDefaultLocale: true,
			redirectToDefaultLocale: false,
		},
	},

	integrations: [
		mdx(),
		sitemap({
			filter: (page) => page !== `${BASE_URL}/admin/`,
			i18n: {
				defaultLocale: DEFAULT_LOCALE_SETTING,
				locales: Object.fromEntries(
					Object.entries(LOCALES_SETTING).map(([key, value]) => [
						key,
						value.lang ?? key,
					]),
				),
			},
		}),
		icon({
			iconDir: "src/assets/icons",
			include: {
				tabler: ["*"],
				openmoji: ["*"],
			},
		}),
		vue(),
	],

  env: {
    schema: {
      BACKEND_URL: envField.string({ context: "client", access: "public", optional: true }),
    }
  },

	vite: {
		plugins: [tailwindcss()],
		resolve: {
			alias: {
				"~": "/src",
				"@cvix/ui": fileURLToPath(
					new URL("../../packages/ui/src", import.meta.url),
				),
				"@cvix/assets": fileURLToPath(
					new URL("../../packages/assets/src", import.meta.url),
				),
			},
		},
		server: {
			https: getHttpsConfig(),
		},
	},
	markdown: {
		remarkPlugins: [remarkReadingTime],
	},
});
