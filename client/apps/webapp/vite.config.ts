import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import type { PluginOption } from "vite";
import { defineConfig } from "vite";
import vueDevTools from "vite-plugin-vue-devtools";

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
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
				ws: true,
				// Preserve cookies between proxy and backend
				cookieDomainRewrite: {
					"*": "",
				},
				configure: (proxy, _options) => {
					proxy.on("error", (err, _req, _res) => {
						console.log("proxy error", err);
					});
					proxy.on("proxyReq", (_proxyReq, req, _res) => {
						console.log("Sending Request to the Target:", req.method, req.url);
						// Log cookies being sent
						if (req.headers.cookie) {
							console.log("  Cookies:", req.headers.cookie);
						}
						// Log CSRF header being sent
						if (req.headers["x-xsrf-token"]) {
							console.log("  CSRF Token:", req.headers["x-xsrf-token"]);
						}
					});
					proxy.on("proxyRes", (proxyRes, req, _res) => {
						console.log(
							"Received Response from the Target:",
							proxyRes.statusCode,
							req.url,
						);
						// Log Set-Cookie headers from backend
						if (proxyRes.headers["set-cookie"]) {
							console.log("  Set-Cookie:", proxyRes.headers["set-cookie"]);
						}
					});
				},
			},
			"/actuator": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
				cookieDomainRewrite: {
					"*": "",
				},
			},
			"/oauth2": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
				cookieDomainRewrite: {
					"*": "",
				},
			},
			"/v3/api-docs": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
				cookieDomainRewrite: {
					"*": "",
				},
			},
		},
	},
});
