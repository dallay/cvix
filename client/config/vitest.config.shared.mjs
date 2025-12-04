import { mergeConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";
import { sharedViteConfig } from "./vite.config.shared.mjs";

/**
 * Legacy shared Vitest configuration.
 *
 * @deprecated This configuration is deprecated in favor of the centralized
 * Vitest projects feature. All global options (reporters, coverage) are now
 * defined in the root vitest.config.ts file.
 *
 * This export is kept for backward compatibility with any packages that might
 * still reference it, but new projects should use defineProject with extends: true
 * to inherit from the root configuration.
 *
 * @see /vitest.config.ts - Root configuration with projects
 * @see /client/apps/webapp/vitest.config.ts - Example project config
 * @see /client/apps/marketing/vitest.config.ts - Example project config
 */
export const sharedVitestConfig = (dirname) =>
	mergeConfig(sharedViteConfig(dirname), {
		test: {
			globals: true,
			include: ["**/__tests__/**/*.spec.{ts,js}", "**/*.spec.{ts,js}"],
			exclude: ["tests/e2e/**/*..spec.e2e.{ts,js}"],
			match: ["**/__tests__/**/*..spec.{ts,js}"],
			// Note: coverage and reporters are now managed centrally in root config
		},
		plugins: [tsconfigPaths()],
	});
