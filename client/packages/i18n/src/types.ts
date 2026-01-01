/**
 * @cvix/i18n - Shared Internationalization Types
 *
 * Framework-agnostic types for internationalization.
 * These types are designed to work with both Astro's i18n and vue-i18n.
 */

/**
 * Supported locale codes in the application.
 * This is the single source of truth for all supported languages.
 */
export type SupportedLocale = "en" | "es";

/**
 * Array of all supported locales for iteration and validation.
 */
export const SUPPORTED_LOCALES: readonly SupportedLocale[] = [
	"en",
	"es",
] as const;

/**
 * Type guard to check if a string is a valid SupportedLocale.
 */
export function isSupportedLocale(value: string): value is SupportedLocale {
	return SUPPORTED_LOCALES.includes(value as SupportedLocale);
}

/**
 * Configuration for a single locale.
 * Contains display information for UI rendering.
 */
export interface LocaleConfig {
	/** Human-readable label for the locale (e.g., "English", "Español") */
	label: string;
	/** BCP 47 language tag (e.g., "en-US", "es-ES") */
	lang: string;
	/** Text direction: left-to-right or right-to-left */
	dir: "ltr" | "rtl";
	/** Icon identifier for flag display (e.g., "openmoji:flag-united-states") */
	flag: string;
	/** Native name of the language in that language */
	nativeName: string;
}

/**
 * Map of all locale configurations keyed by locale code.
 */
export type LocalesConfig = {
	[K in SupportedLocale]: LocaleConfig;
};

/**
 * Language option for dropdown/selector components.
 * Simplified interface for UI language selectors.
 */
export interface LanguageOption {
	/** Display name of the language */
	name: string;
	/** Locale code (e.g., "en", "es") */
	code: SupportedLocale;
	/** Optional flag icon identifier */
	flag?: string;
}

/**
 * Generic translation dictionary type.
 * Used for flat key-value translation structures.
 */
export type TranslationDict = Record<string, string>;

/**
 * Nested translation object type.
 * Used for hierarchical translation structures (vue-i18n style).
 */
export type NestedTranslations = {
	[key: string]: string | NestedTranslations;
};

/**
 * Multilingual translation structure (Astro style).
 * Maps each locale to its translation dictionary.
 */
export type MultilingualTranslations<T = TranslationDict> = {
	[K in SupportedLocale]: T;
};
