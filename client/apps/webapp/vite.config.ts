import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import type { PluginOption } from "vite";
import { defineConfig } from "vite";
import vueDevTools from "vite-plugin-vue-devtools";

// Make backend proxy target configurable via BACKEND_URL env var (fallback: http://localhost:8080)
const backendTarget: string =
	process.env.BACKEND_URL ?? "http://localhost:8080";
// https://vite.dev/config/
export default defineConfig({
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
			"~icons": "virtual:icons",
		},
	},
	optimizeDeps: {
		exclude: ["@cvix/utilities"],
	},
	define: {
		I18N_HASH: '"generated_hash"',
		SERVER_API_URL: '"/"',
		APP_VERSION: `"${process.env.APP_VERSION ? process.env.APP_VERSION : "DEV"}"`,
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
					proxy.on("proxyRes", (proxyRes, _req, _res) => {
						// Log Set-Cookie headers from backend
						if (proxyRes.headers["set-cookie"]) {
							console.log(
								"Set-Cookie headers from backend:",
								proxyRes.headers["set-cookie"],
							);
						}
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
});
