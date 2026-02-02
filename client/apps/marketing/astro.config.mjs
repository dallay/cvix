import mdx from "@astrojs/mdx";
import sitemap from "@astrojs/sitemap";
import vue from "@astrojs/vue";
import { envSchema } from "@cvix/astro-ui/env";
import { CVIX_MARKETING_URL, PORTS } from "@cvix/lib";
import {
	CVIX_SSR_NO_EXTERNAL,
	createI18nConfig,
	createSitemapConfig,
	DEFAULT_ICON_CONFIG,
	DEFAULT_IMAGE_CONFIG,
	getHttpsConfig,
} from "@cvix/lib/astro";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "astro/config";
import icon from "astro-icon";

// https://astro.build/config
export default defineConfig({
	server: {
		port: PORTS.MARKETING,
	},

	image: DEFAULT_IMAGE_CONFIG,

	site: CVIX_MARKETING_URL,

	i18n: createI18nConfig(),

	integrations: [
		mdx(),
		sitemap(createSitemapConfig({ siteUrl: CVIX_MARKETING_URL })),
		icon(DEFAULT_ICON_CONFIG),
		vue(),
	],

	env: {
		schema: envSchema,
	},

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
