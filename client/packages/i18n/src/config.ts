/**
 * @cvix/i18n - Locale Configuration
 *
 * Single source of truth for all locale-related configuration.
 * This module provides the canonical locale settings used across all apps.
 */

import type {
	LanguageOption,
	LocaleConfig,
	LocalesConfig,
	SupportedLocale,
} from "./types.js";
import { SUPPORTED_LOCALES } from "./types.js";

/**
 * Default locale for the application.
 * Used as fallback when no locale is specified or detected.
 */
export const DEFAULT_LOCALE: SupportedLocale = "en";

/**
 * Complete configuration for all supported locales.
 * This is the single source of truth for locale metadata.
 */
export const LOCALES: LocalesConfig = {
	en: {
		label: "English",
		lang: "en",
		dir: "ltr",
		flag: "openmoji:flag-united-states",
		nativeName: "English",
	},
	es: {
		label: "Español",
		lang: "es",
		dir: "ltr",
		flag: "openmoji:flag-spain",
		nativeName: "Español",
	},
} as const;

/**
 * Language options for UI selectors (dropdowns, menus, etc.).
 * Pre-formatted array ready for use in components.
 */
export const LANGUAGES: readonly LanguageOption[] = SUPPORTED_LOCALES.map(
	(code) => ({
		name: LOCALES[code].label,
		code,
		flag: LOCALES[code].flag,
	}),
);

/**
 * Get the configuration for a specific locale.
 * @param locale - The locale code to get configuration for
 * @returns The locale configuration object
 */
export function getLocaleConfig(locale: SupportedLocale): LocaleConfig {
	return LOCALES[locale];
}

/**
 * Get the BCP 47 language tag for a locale.
 * Useful for setting the `lang` attribute on HTML elements.
 * @param locale - The locale code
 * @returns The BCP 47 language tag (e.g., "en-US")
 */
export function getLanguageTag(locale: SupportedLocale): string {
	return LOCALES[locale].lang;
}

/**
 * Get the text direction for a locale.
 * @param locale - The locale code
 * @returns "ltr" or "rtl"
 */
export function getTextDirection(locale: SupportedLocale): "ltr" | "rtl" {
	return LOCALES[locale].dir;
}

/**
 * Validate and normalize a locale string.
 * Returns the locale if valid, or the default locale if invalid.
 * @param locale - The locale string to validate
 * @returns A valid SupportedLocale
 */
export function normalizeLocale(
	locale: string | undefined | null,
): SupportedLocale {
	if (locale && SUPPORTED_LOCALES.includes(locale as SupportedLocale)) {
		return locale as SupportedLocale;
	}
	return DEFAULT_LOCALE;
}

/**
 * Extracts the base locale from a BCP 47 language tag and returns it as a supported locale.
 *
 * Example: `"en-US"` becomes `"en"`.
 *
 * @param languageTag - A BCP 47 language tag (for example, `"en-US"`, `"es-ES"`).
 * @returns The normalized base locale code; returns `DEFAULT_LOCALE` when the extracted base is not a supported locale.
 * @example
 * // returns "en"
 * extractBaseLocale("en-US");
 */
export function extractBaseLocale(languageTag: string): SupportedLocale {
	const baseLocale = languageTag.split("-")[0]?.toLowerCase();
	return normalizeLocale(baseLocale);
}

export type { LanguageOption, LocaleConfig, SupportedLocale } from "./types.js";
// Re-export types and constants for convenience
export { SUPPORTED_LOCALES } from "./types.js";