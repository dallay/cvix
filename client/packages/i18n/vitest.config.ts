import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { defineProject } from "vitest/config";

const projectRoot = dirname(fileURLToPath(import.meta.url));

export default defineProject({
	test: {
		// Project-specific identification
		name: { label: "i18n", color: "blue" },

		// Node environment for internationalization library testing
		environment: "node",

		include: ["src/**/*.{test,spec}.{js,ts}"],
		exclude: ["node_modules", "dist"],
	},
	resolve: {
		alias: {
			"@": resolve(projectRoot, "./src"),
		},
	},
});
