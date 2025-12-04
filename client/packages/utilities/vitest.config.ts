
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import { defineProject } from "vitest/config";

/**
 * Utilities package project configuration.
 * Projects automatically inherit global options (reporters, coverage) from root vitest.config.ts.
 * This config provides Node library-specific overrides.
 */

const projectRoot = dirname(fileURLToPath(import.meta.url));

export default defineProject({
    test: {
        // Project-specific identification
        name: { label: "utilities", color: "green" },

        // Node environment for utility library testing
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
