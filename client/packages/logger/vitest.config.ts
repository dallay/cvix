import { resolve } from "node:path";
import { sharedVitestConfig } from "@loomify/config/vitest.config.shared";
import { defineConfig, mergeConfig } from "vite";

export default defineConfig(
	mergeConfig(sharedVitestConfig(__dirname), {
		test: {
			environment: "jsdom",
			globals: true,
			include: ["tests/**/*.{test,spec}.ts"],
			exclude: ["node_modules", "dist", "tests/e2e/**/*.spec.e2e.ts"],
			testTimeout: 10000,
			coverage: {
				provider: "v8",
				enabled: true,
				reportsDirectory: "./coverage",
				include: ["src/**/*.ts"],
				exclude: [
					"tests/**/*.{test,spec}.ts",
					"src/examples/**",
					"src/index.ts",
					"src/**/*.d.ts",
				],
				thresholds: {
					global: {
						branches: 80,
						functions: 80,
						lines: 80,
						statements: 80,
					},
				},
				reporter: ["text", "json", "html"],
			},
		},
		resolve: {
			alias: {
				"@": resolve(__dirname, "./src"),
			},
		},
	}),
);
