import { defineConfig } from "vitest/config";

/**
 * Centralized Vitest configuration for the monorepo.
 * Uses the projects feature to manage multiple test contexts.
 *
 * Each project inherits global options like reporters and coverage settings,
 * but can override environment, plugins, and other project-specific options.
 */
export default defineConfig({
  test: {
    // Global test options that apply to all projects
    globals: true,

    // Global reporters - only defined at root level per Vitest docs
    reporters: [
      "default",
      ["json", { file: "./test-results/json-report.json" }],
      [
        "junit",
        {
          suiteName: "Monorepo Tests",
          outputFile: "./test-results/junit-report.xml",
        },
      ],
    ],

    // Global coverage configuration - only at root level
    coverage: {
      provider: "v8",
      enabled: true,
      reportsDirectory: "./coverage",
      reporter: ["text", "json-summary", "lcov", "html"],
      exclude: [
        // Standard exclusions
        "**/node_modules/**",
        "**/dist/**",
        "**/.{idea,git,cache,output,temp}/**",
        "**/{karma,rollup,webpack,vite,vitest,jest,ava,babel,nyc,cypress,tsup,build,eslint}.config.*",
        "**/__tests__/**",
        "**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}",
        "**/components.d.ts",
        "**/*.d.ts",
        "**/index.ts",
        "e2e/**",
        // Marketing app: Astro-generated files
        "client/apps/marketing/src/env.d.ts",
        "client/apps/marketing/src/consts.ts",
        "client/apps/marketing/src/content.config.ts",
        "client/apps/marketing/src/pages/robots.txt.ts",
        "client/apps/marketing/src/i18n/**",
      ],
    },

    // Define projects for monorepo structure
    projects: [
      // Reference project configs by path
      "./client/apps/webapp",
      "./client/apps/marketing",
      "./client/packages/utilities",
    ],
  },
});
