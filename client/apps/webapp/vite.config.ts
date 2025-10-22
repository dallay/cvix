import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
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
		}),
		Icons({
			autoInstall: true,
			compiler: "vue3",
		}),
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
				configure: (proxy, _options) => {
					proxy.on("error", (err, _req, _res) => {
						console.log("proxy error", err);
					});
					proxy.on("proxyReq", (_proxyReq, req, _res) => {
						console.log("Sending Request to the Target:", req.method, req.url);
					});
					proxy.on("proxyRes", (proxyRes, req, _res) => {
						console.log(
							"Received Response from the Target:",
							proxyRes.statusCode,
							req.url,
						);
					});
				},
			},
			"/actuator": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
			},
			"/oauth2": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
			},
			"/login": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
			},
			"/v3/api-docs": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
			},
			"/h2-console": {
				target: "http://localhost:8080",
				secure: false,
				changeOrigin: true,
			},
		},
	},
});
