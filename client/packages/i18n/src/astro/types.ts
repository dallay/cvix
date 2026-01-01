/**
 * @cvix/i18n/astro - Astro-specific i18n Types
 *
 * Types for internationalization in Astro applications.
 * These types extend the base i18n types with Astro-specific functionality.
 */

import type { SupportedLocale } from "../types";

/**
 * Language code type derived from supported locales.
 * Use this for Astro route parameters and i18n functions.
 */
export type Lang = SupportedLocale;

/**
 * Dictionary of UI translations for a single language.
 * Maps translation keys to their translated strings.
 */
export type UIDict = Record<string, string>;

/**
 * Complete UI translations for all supported languages.
 * Maps each language code to its translation dictionary.
 */
export type UIMultilingual = { [K in Lang]: UIDict };

/**
 * Multilingual content object.
 * Used for content that has translations in multiple languages.
 *
 * @example
 * const title: Multilingual = {
 *   en: "Welcome",
 *   es: "Bienvenido"
 * };
 */
export type Multilingual = { [K in Lang]?: string };

/**
 * Generic translations type for organizing translations by namespace.
 */
export type Translations = Record<string, Record<string, string>>;

/**
 * Locale path for language switching.
 * Used by getLocalePaths to generate alternate language URLs.
 */
export type LocalePath = {
	lang: Lang;
	path: string;
};

/**
 * Astro-specific locale configuration.
 * Extends base config with optional flag for UI display.
 */
export type AstroLocaleConfig = {
	readonly label: string;
	readonly lang?: string;
	readonly dir?: "ltr" | "rtl";
	readonly flag?: string;
};

/**
 * Map of locale configurations keyed by locale code.
 */
export type AstroLocalesConfig = Record<string, AstroLocaleConfig>;

/**
 * Translation function type returned by useTranslations.
 */
export type TranslationFunction = (
	multilingualOrKey: Multilingual | string,
	variables?: Record<string, string | number>,
) => string;

/**
 * Path translation function type returned by useTranslatedPath.
 */
export type TranslatePathFunction = (path: string, targetLang?: Lang) => string;
