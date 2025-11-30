import { fileURLToPath, URL } from "node:url";
import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import IconsResolver from "unplugin-icons/resolver";
import Icons from "unplugin-icons/vite";
import Components from "unplugin-vue-components/vite";
import { loadEnv, type PluginOption } from "vite";
import { configDefaults, defineConfig } from "vitest/config";

const projectRoot = fileURLToPath(new URL(".", import.meta.url));

export default defineConfig(({ mode }) => {
	// Load env from root first (as fallback)
	const rootEnv = loadEnv(mode, `${process.cwd()}/../../..`, "");

	// Then load local env (will override root env if variables exist locally)
	const localEnv = loadEnv(mode, process.cwd(), "");

	// Merge env variables: local takes precedence over root
	const env = { ...rootEnv, ...localEnv };

	return {
		root: projectRoot,
		plugins: [
			vue(),
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
			APP_VERSION: `"${env.APP_VERSION ? env.APP_VERSION : "DEV"}"`,
		},
		test: {
			environment: "jsdom",
			exclude: [...configDefaults.exclude, "e2e/**"],
			root: projectRoot,
			setupFiles: ["./vitest.setup.ts"],
			coverage: {
				provider: "v8",
				reporter: ["text", "json", "html", "lcov"],
				exclude: [
					...(configDefaults.coverage.exclude ?? []),
					"e2e/**",
					"**/*.config.*",
					"**/*.d.ts",
					"**/index.ts",
					"**/__tests__/**",
					"**/tests/**",
				],
				thresholds: {
					lines: 80,
					branches: 80,
					functions: 80,
					statements: 80,
				},
			},
		},
	};
});
