// @ts-check
import { existsSync } from "node:fs";
import { fileURLToPath, URL } from "node:url";
import mdx from "@astrojs/mdx";
import sitemap from "@astrojs/sitemap";
import vue from "@astrojs/vue";
import { DEFAULT_LOCALE, LOCALES } from "@cvix/i18n";
import { CVIX_API_URL, CVIX_BLOG_URL, CVIX_WEBAPP_URL, PORTS } from "@cvix/lib";
import { SSL_CERT_PATH, SSL_KEY_PATH } from "@cvix/lib/ssl";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig, envField } from "astro/config";
import icon from "astro-icon";
import { remarkReadingTime } from "./src/utils/remark-reading-time.mjs";

/**
 * Check if SSL certificates exist for HTTPS development
 * @returns {boolean} true if both cert and key files exist
 */
function hasSSLCertificates() {
	const certExists = existsSync(SSL_CERT_PATH);
	const keyExists = existsSync(SSL_KEY_PATH);
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
		port: PORTS.BLOG,
	},

	// Configure Sharp image service explicitly
	image: {
		service: {
			entrypoint: "astro/assets/services/sharp",
			config: {
				limitInputPixels: false,
			},
		},
	},

	site: CVIX_BLOG_URL,

	i18n: {
		defaultLocale: DEFAULT_LOCALE,
		locales: Object.keys(LOCALES),
		routing: {
			prefixDefaultLocale: true,
			redirectToDefaultLocale: false,
		},
	},

	integrations: [
		mdx(),
		sitemap({
			filter: (page) => page !== `${CVIX_BLOG_URL}/admin/`,
			i18n: {
				defaultLocale: DEFAULT_LOCALE,
				locales: Object.fromEntries(
					Object.entries(LOCALES).map(([key, value]) => [
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
			CVIX_API_URL: envField.string({
				context: "client",
				access: "public",
				optional: true,
				default: CVIX_API_URL,
			}),
			CVIX_WEBAPP_URL: envField.string({
				context: "client",
				access: "public",
				default: CVIX_WEBAPP_URL,
			}),
		},
	},

	vite: {
		// @ts-expect-error - Type mismatch between Astro's bundled Vite 6.x and monorepo's Vite 7.x
		plugins: [tailwindcss()],
		ssr: {
			noExternal: [
				"@cvix/assets",
				"@cvix/astro-ui",
				"@cvix/i18n",
				"@cvix/lib",
				"@cvix/tsconfig",
				"@cvix/ui",
				"@cvix/utilities",
			],
		},
		server: {
			https: getHttpsConfig(),
		},
	},

	markdown: {
		remarkPlugins: [remarkReadingTime],
	},
});
