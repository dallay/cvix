import { mergeConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";
import { sharedViteConfig } from "./vite.config.shared.mjs";
export const sharedVitestConfig = (dirname) =>
	mergeConfig(sharedViteConfig(dirname), {
		test: {
			// run the tests inside the folder __tests__ and skip the ones .spec.e2e files
			globals: true,
			include: ["**/__tests__/**/*.spec.{ts,js}", "**/*.spec.{ts,js}"],
			exclude: ["tests/e2e/**/*..spec.e2e.{ts,js}"],
			match: ["**/__tests__/**/*..spec.{ts,js}"],
			coverage: {
				provider: "v8",
				enabled: true,
				reportsDirectory: "./coverage",
			},
			reporters: [
				"default",
				["json", { file: "./coverage/json-report.json" }],
				"verbose",
				[
					"junit",
					{ suiteName: "UI tests", outputFile: "./coverage/junit-report.xml" },
				],
			],
		},
		plugins: [tsconfigPaths()],
	});
