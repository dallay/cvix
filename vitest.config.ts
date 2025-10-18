import { resolve } from "node:path";
import { defineConfig } from "vitest/config";

export default defineConfig({
	test: {
		globals: true,
		environment: "node",
		include: [
			"**/__tests__/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}",
			"**/*.{spec,test}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}",
			"client/packages/logger/src/**/*.ts",
		],
		exclude: [
			"**/node_modules/**",
			"**/dist/**",
			"**/cypress/**",
			"**/.{idea,git,cache,output,temp}/**",
			"**/{karma,rollup,webpack,vite,vitest,jest,ava,babel,nyc,cypress,tsup,build,eslint,prettier}.config.*",
			"**/components.d.ts",
		],
		setupFiles: ["./vitest.setup.ts"],
		coverage: {
			provider: "v8", // or 'istanbul'
			reporter: ["text"], // 'lcov' is not supported by 'v8'
			reportsDirectory: "./coverage",
			include: ["src/**/*.{js,ts,vue,jsx,tsx}"], // Adjust as needed
			exclude: [
				"src/env.d.ts",
				"src/consts.ts",
				"src/content.config.ts",
				"src/pages/robots.txt.ts", // Example: if this is auto-generated or not testable
				"src/__tests__/**", // Test files themselves
				"**/__tests__/*.(test|spec).{js,mjs,cjs,ts,mts,cts,jsx,tsx}", // Explicitly exclude test files
				"src/i18n/**", // if i18n setup is not directly tested
			],
		},
	},
	resolve: {
		alias: {
			"@": resolve(__dirname, "./src"),
			"@i18n": resolve(__dirname, "./src/i18n"),
			"@lib": resolve(__dirname, "./src/lib"),
			"@models": resolve(__dirname, "./src/lib/models"),
			"@components": resolve(__dirname, "./src/components"),
		},
	},
});
