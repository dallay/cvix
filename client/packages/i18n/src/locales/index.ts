/**
 * @cvix/i18n - Common Locales
 *
 * Shared translations that are common across all applications.
 * These can be imported and merged with app-specific translations.
 */

import type { NestedTranslations, SupportedLocale } from "../types";

// Import common translations
import en from "./common/en.json" with { type: "json" };
import es from "./common/es.json" with { type: "json" };

/**
 * Common translations for all supported locales.
 * These translations are framework-agnostic and can be used
 * with vue-i18n, astro:i18n, or any other i18n library.
 */
export const commonTranslations: Record<SupportedLocale, NestedTranslations> = {
	en,
	es,
} as const;

/**
 * Get common translations for a specific locale.
 * @param locale - The locale code
 * @returns The nested translation object for that locale
 */
export function getCommonTranslations(
	locale: SupportedLocale,
): NestedTranslations {
	return commonTranslations[locale];
}

/**
 * Get a flat version of common translations for Astro-style i18n.
 * Converts nested structure to dot-notation keys.
 *
 * Example:
 * { common: { loading: "Loading..." } }
 * becomes:
 * { "common.loading": "Loading..." }
 *
 * @param locale - The locale code
 * @returns Flat translation dictionary with dot-notation keys
 */
export function getFlatTranslations(
	locale: SupportedLocale,
): Record<string, string> {
	const nested = commonTranslations[locale];
	return flattenObject(nested);
}

/**
 * Flatten a nested object into dot-notation keys.
 * @internal
 */
function flattenObject(
	obj: NestedTranslations,
	prefix = "",
): Record<string, string> {
	const result: Record<string, string> = {};

	for (const [key, value] of Object.entries(obj)) {
		const newKey = prefix ? `${prefix}.${key}` : key;

		if (typeof value === "string") {
			result[newKey] = value;
		} else {
			Object.assign(result, flattenObject(value, newKey));
		}
	}

	return result;
}

// Re-export JSON files for direct import if needed
export { en, es };
