// @ts-check
import mdx from "@astrojs/mdx";
import sitemap from "@astrojs/sitemap";
import vue from "@astrojs/vue";
import { envSchema } from "@cvix/astro-ui/env";
import { CVIX_BLOG_URL, PORTS } from "@cvix/lib";
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
import { remarkReadingTime } from "./src/utils/remark-reading-time.mjs";

// https://astro.build/config
export default defineConfig({
	server: {
		port: PORTS.BLOG,
	},

	image: DEFAULT_IMAGE_CONFIG,

	site: CVIX_BLOG_URL,

	i18n: createI18nConfig(),

	integrations: [
		mdx(),
		sitemap(createSitemapConfig({ siteUrl: CVIX_BLOG_URL })),
		icon(DEFAULT_ICON_CONFIG),
		vue(),
	],

	env: {
		schema: envSchema,
	},

	vite: {
		// @ts-expect-error - Type mismatch between Astro's bundled Vite 6.x and monorepo's Vite 7.x
		plugins: [tailwindcss()],
		ssr: {
			noExternal: [...CVIX_SSR_NO_EXTERNAL],
		},
		server: {
			https: getHttpsConfig(),
		},
	},

	markdown: {
		remarkPlugins: [remarkReadingTime],
	},
});
