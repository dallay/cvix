/** @type {import("vite").UserConfig} */
import { resolve } from "node:path";
import { codecovVitePlugin } from "@codecov/vite-plugin";
export const sharedViteConfig = (dirname) => ({
	resolve: {
		alias: {
			"~": resolve(dirname, "src"),
			"@": resolve(dirname, "src"),
		},
		mainFields: ["module"],
	},
	plugins: [
		codecovVitePlugin({
			enableBundleAnalysis: process.env.CODECOV_TOKEN !== undefined,
			bundleName: "cvix",
			uploadToken: process.env.CODECOV_TOKEN,
		}),
	],
	test: {
		globals: true,
		environment: "happy-dom",
		alias: {
			"@/": resolve(dirname, "src/"),
		},
	},
});
