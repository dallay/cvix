import { DEFAULT_LOCALE, LOCALES } from "@cvix/i18n";

/**
 * Default i18n routing configuration for Astro apps.
 */
export const DEFAULT_I18N_ROUTING = {
	prefixDefaultLocale: true,
	redirectToDefaultLocale: false,
} as const;

/**
 * Creates the standard i18n configuration for Astro apps.
 *
 * @returns Astro i18n configuration object
 */
export function createI18nConfig() {
	return {
		defaultLocale: DEFAULT_LOCALE,
		locales: Object.keys(LOCALES),
		routing: DEFAULT_I18N_ROUTING,
	} as const;
}
