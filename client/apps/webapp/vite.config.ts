import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import type { PluginOption } from "vite";
import { defineConfig, loadEnv } from "vite";
import vueDevTools from "vite-plugin-vue-devtools";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
	// Load env from root first (as fallback)
	// see https://vitejs.dev/config/shared-options.html#envdir
	const rootEnv = loadEnv(mode, `${process.cwd()}/../../..`, "");

	// Then load local env (will override root env if variables exist locally)
	const localEnv = loadEnv(mode, process.cwd(), "");

	// Merge env variables: local takes precedence over root
	const env = { ...rootEnv, ...localEnv };

	// Make backend proxy target configurable via BACKEND_URL env var (fallback: http://localhost:8080)
	const backendTarget: string = env.BACKEND_URL ?? "http://localhost:8080";

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
			proxy: {
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
						proxy.on("proxyRes", (_proxyRes, _req, _res) => {
							// console logging removed to avoid leaking headers in dev output
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
