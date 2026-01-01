import { resolve } from "node:path";
import { sharedViteConfig } from "@cvix/config/vite.config.shared";
import { defineConfig, mergeConfig } from "vite";
import dts from "vite-plugin-dts"; // 1. Importa el plugin

/** @type {import('vite').UserConfig} */
export default defineConfig(
	mergeConfig(sharedViteConfig(__dirname), {
		build: {
			lib: {
				entry: resolve(__dirname, "src/index.ts"),
				name: "utilities",
				formats: ["es"],
				// El nombre del archivo se infiere del `package.json` (main/module)
				// por lo que no necesitas `fileName` aquí.
			},
			target: "esnext",
		},
		plugins: [
			// 2. Añade el plugin y apúntalo a tu tsconfig de build
			dts({
				tsconfigPath: "./tsconfig.build.json",
			}),
		],
	}),
);
