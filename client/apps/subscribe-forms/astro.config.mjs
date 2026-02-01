import node from "@astrojs/node";
import { envSchema } from "@cvix/astro-ui/env";
import { CVIX_SUBSCRIBE_FORMS_URL, PORTS } from "@cvix/lib";
import {
	CVIX_SSR_NO_EXTERNAL,
	DEFAULT_ICON_CONFIG,
	getHttpsConfig,
} from "@cvix/lib/astro";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "astro/config";
import icon from "astro-icon";

export default defineConfig({
	site: CVIX_SUBSCRIBE_FORMS_URL,
	server: {
		port: PORTS.SUBSCRIBE_FORMS,
	},

	// SSR mode required for dynamic form loading
	output: "server",
	adapter: node({
		mode: "standalone",
	}),

	env: {
		schema: envSchema,
	},

	integrations: [icon(DEFAULT_ICON_CONFIG)],

	vite: {
		plugins: [tailwindcss()],
		ssr: {
			noExternal: [...CVIX_SSR_NO_EXTERNAL],
		},
		server: {
			https: getHttpsConfig(),
		},
	},
});
