import { existsSync } from "node:fs";
import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import type { PluginOption } from "vite";
import { defineConfig, loadEnv } from "vite";
import vueDevTools from "vite-plugin-vue-devtools";

/**
 * Check if SSL certificates exist for HTTPS development
 * @returns {boolean} true if both cert and key files exist
 */
function hasSSLCertificates(): boolean {
	const certPath = fileURLToPath(
		new URL("../../../infra/ssl/localhost.pem", import.meta.url),
	);
	const keyPath = fileURLToPath(
		new URL("../../../infra/ssl/localhost-key.pem", import.meta.url),
	);

	const certExists = existsSync(certPath);
	const keyExists = existsSync(keyPath);

	return certExists && keyExists;
}

/**
 * Get HTTPS configuration for Vite dev server
 * Falls back to HTTP if certificates are not available
 * @returns HTTPS config or undefined for HTTP
 */
function getHttpsConfig(): { key: string; cert: string } | undefined {
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
		key: fileURLToPath(
			new URL("../../../infra/ssl/localhost-key.pem", import.meta.url),
		),
		cert: fileURLToPath(
			new URL("../../../infra/ssl/localhost.pem", import.meta.url),
		),
	};
}

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
	// Load env from root first (as fallback)
	// see https://vitejs.dev/config/shared-options.html#envdir
	const rootEnv = loadEnv(mode, `${process.cwd()}/../../..`, "");

	// Then load local env (will override root env if variables exist locally)
	const localEnv = loadEnv(mode, process.cwd(), "");

	// Merge env variables: local takes precedence over root
	const env = { ...rootEnv, ...localEnv };

	// Make backend proxy target configurable via CVIX_API_URL env var (fallback: http://localhost:8080)
	const backendTarget: string = env.CVIX_API_URL ?? "http://localhost:8080";

	// Disable proxy during E2E tests when using mocked APIs (manual mocking or HAR files)
	// Playwright handles the mocking at the browser level, so Vite proxy should be bypassed
	const isPlaywrightTest = process.env.PLAYWRIGHT_TEST === "true";
	const disableProxy = isPlaywrightTest;

	return {
		plugins: [
			vue(),
			vueDevTools(),
			tailwindcss(),
			Components({
				dts: true,
				resolvers: [
					IconsResolver({
						prefix: "",
						enabledCollections: ["ph"],
					}),
				],
			}) as PluginOption,
			Icons({
				autoInstall: true,
				compiler: "vue3",
			}) as PluginOption,
		],
		resolve: {
			alias: {
				"@": fileURLToPath(new URL("./src", import.meta.url)),
				"@cvix/ui": fileURLToPath(
					new URL("../../packages/ui/src", import.meta.url),
				),
				"@cvix/assets": fileURLToPath(
					new URL("../../packages/assets/src", import.meta.url),
				),
				"~icons": "virtual:icons",
			},
		},
		optimizeDeps: {
			exclude: ["@cvix/utilities"],
		},
		define: {
			I18N_HASH: '"generated_hash"',
			SERVER_API_URL: '"/"',
			APP_VERSION: `"${env.APP_VERSION ? env.APP_VERSION : "DEV"}"`,
		},
		server: {
			host: true,
			port: 9876,
			https: getHttpsConfig(),
			// Disable proxy during E2E tests - Playwright handles API mocking
			proxy: disableProxy
				? undefined
				: {
						"/api": {
							target: backendTarget,
							secure: false,
							changeOrigin: true,
							ws: true,
							// Preserve cookies between proxy and backend
							cookieDomainRewrite: {
								"*": "",
							},
							configure: (proxy, _options) => {
								proxy.on("error", (err, _req, _res) => {
									console.error("proxy error", err);
								});
							},
						},
						"/actuator": {
							target: backendTarget,
							secure: false,
							changeOrigin: true,
							cookieDomainRewrite: {
								"*": "",
							},
						},
						"/oauth2": {
							target: backendTarget,
							secure: false,
							changeOrigin: true,
							cookieDomainRewrite: {
								"*": "",
							},
						},
						"/v3/api-docs": {
							target: backendTarget,
							secure: false,
							changeOrigin: true,
							cookieDomainRewrite: {
								"*": "",
							},
						},
					},
		},
	};
});
