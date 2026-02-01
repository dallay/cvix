import { DEFAULT_LOCALE, LOCALES } from "@cvix/i18n";

/**
 * Type for the icon configuration
 */
export type IconConfig = {
	iconDir: string;
	include: Record<string, string[]>;
};

/**
 * Default configuration for astro-icon integration.
 * Includes tabler and openmoji icon sets.
 */
export const DEFAULT_ICON_CONFIG: IconConfig = {
	iconDir: "src/assets/icons",
	include: {
		tabler: ["*"],
		openmoji: ["*"],
	},
};

/**
 * Configuration options for creating sitemap config
 */
export type SitemapConfigOptions = {
	/** Base site URL */
	siteUrl: string;
	/** Pages to exclude from sitemap (e.g., '/admin/') */
	excludePages?: string[];
};

/**
 * Creates a sitemap configuration with i18n support.
 *
 * @param options - Sitemap configuration options
 * @returns Configuration object for @astrojs/sitemap
 */
export function createSitemapConfig(options: SitemapConfigOptions) {
	const { siteUrl, excludePages = ["/admin/"] } = options;

	return {
		filter: (page: string) =>
			!excludePages.some((excluded) => page === `${siteUrl}${excluded}`),
		i18n: {
			defaultLocale: DEFAULT_LOCALE,
			locales: Object.fromEntries(
				Object.entries(LOCALES).map(([key, value]) => [key, value.lang ?? key]),
			),
		},
	};
}
