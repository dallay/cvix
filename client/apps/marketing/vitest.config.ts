import { resolve } from "node:path";
import { defineConfig } from "vitest/config";

export default defineConfig({
	test: {
		globals: true,
		environment: "node",
		include: ["**/__tests__/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
		exclude: ["node_modules", "dist", ".idea", ".git", ".cache"],
		setupFiles: ["./vitest.setup.ts"],
		coverage: {
			// Add this section
			provider: "v8", // or 'istanbul'
			reporter: ["text", "lcov"], // Ensure lcov is present
			reportsDirectory: "./coverage", // Default is 'coverage'
			include: ["src/**/*.{js,ts,vue,jsx,tsx}"], // Adjust as needed
			exclude: [
				// Optional: exclude files if necessary
				"src/env.d.ts",
				"src/consts.ts",
				"src/content.config.ts",
				"src/pages/robots.txt.ts", // Example: if this is auto-generated or not testable
				"src/**/__tests__/**", // Test files themselves
				"src/i18n/**", // if i18n setup is not directly tested
			],
		},
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
