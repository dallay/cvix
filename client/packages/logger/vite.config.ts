import { resolve } from "node:path";
import { sharedViteConfig } from "@loomify/config/vite.config.shared";
import { defineConfig, mergeConfig } from "vite";
import dts from "vite-plugin-dts";

/** @type {import('vite').UserConfig} */
export default defineConfig(
	mergeConfig(sharedViteConfig(__dirname), {
		test: {
			include: ["tests/**/*.test.ts"],
		},
		resolve: {
			"@": resolve(__dirname, "./src"),
		},
		build: {
			lib: {
				entry: resolve(__dirname, "src/index.ts"),
				name: "Logger",
			},
			target: ["es2022", "node18"],
			rollupOptions: {
				// Exclude Node.js built-in modules and future peer dependencies from the bundle
				external: [
					"fs",
					"path",
					"os",
					"util",
					"stream",
					"events",
					"http",
					"https",
					"url",
					"crypto",
					"zlib",
					"buffer",
					"child_process",
					"net",
					"tls",
					"dns",
					"readline",
					"repl",
					"vm",
					"worker_threads",
					"assert",
					"tty",
					"module",
					"process",
					// Add any peer dependencies here, e.g. "some-peer-lib"
				],
				output: [
					{
						format: "es",
						preserveModules: false,
						exports: "named",
						entryFileNames: "logger.mjs",
					},
					{
						format: "cjs",
						preserveModules: false,
						exports: "named",
						entryFileNames: "logger.cjs",
					},
				],
			},
			minify: process.env.NODE_ENV === "production",
			sourcemap: true,
		},
		plugins: [
			dts({
				tsconfigPath: "./tsconfig.build.json",
				insertTypesEntry: true,
				rollupTypes: true,
			}),
		],
	}),
);
