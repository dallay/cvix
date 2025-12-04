import { resolve } from "node:path";
import { defineProject } from "vitest/config";

/**
 * Marketing site project configuration.
 * Projects automatically inherit global options (reporters, coverage) from root vitest.config.ts.
 * This config provides Astro/Node-specific overrides.
 */
export default defineProject({
	test: {
		// Project-specific identification
		name: { label: "marketing", color: "cyan" },

		// Astro SSR testing requires Node environment
		environment: "node",

		include: ["**/__tests__/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
		exclude: ["node_modules", "dist", ".idea", ".git", ".cache"],
		setupFiles: ["./vitest.setup.ts"],
	},
	resolve: {
		alias: {
			"@": resolve(__dirname, "./src"),
			"@i18n": resolve(__dirname, "./src/i18n/index.ts"),
			"@lib": resolve(__dirname, "./src/lib"),
			"@models": resolve(__dirname, "./src/lib/models"),
			"@components": resolve(__dirname, "./src/components"),
		},
	},
});
