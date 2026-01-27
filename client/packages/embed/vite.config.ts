import { resolve } from "path";
import { defineConfig } from "vite";
import tsconfigPaths from "vite-tsconfig-paths";

export default defineConfig({
	plugins: [tsconfigPaths()],
	build: {
		lib: {
			entry: {
				loader: resolve(__dirname, "src/loader.ts"),
				embed: resolve(__dirname, "src/embed.ts"),
			},
			formats: ["es"],
			fileName: (format, entryName) => `${entryName}.js`,
		},
		rollupOptions: {
			output: {
				manualChunks: undefined,
			},
		},
	},
});
