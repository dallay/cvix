import path from "node:path";
import { fileURLToPath } from "node:url";
import { configDefaults, defineConfig, mergeConfig } from "vitest/config";
import viteConfig from "./vite.config";

const projectRoot = fileURLToPath(new URL(".", import.meta.url));

export default mergeConfig(
	viteConfig,
	defineConfig({
		root: projectRoot,
		resolve: {
			alias: {
				"@": path.resolve(projectRoot, "src"),
			},
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
	}),
);
