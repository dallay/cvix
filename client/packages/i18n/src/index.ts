/**
 * @cvix/i18n - Shared Internationalization Package
 *
 * This package provides framework-agnostic i18n utilities and configurations
 * that can be used across all CVIX applications (Astro, Vue, etc.).
 *
 * @example
 * ```typescript
 * // Import configuration
 * import { DEFAULT_LOCALE, LOCALES, LANGUAGES } from '@cvix/i18n';
 *
 * // Import types
 * import type { SupportedLocale, LocaleConfig } from '@cvix/i18n';
 *
 * // Import common translations
 * import { getCommonTranslations } from '@cvix/i18n/locales';
 * ```
 */

// Configuration
export {
	DEFAULT_LOCALE,
	extractBaseLocale,
	getLanguageTag,
	getLocaleConfig,
	getTextDirection,
	LANGUAGES,
	LOCALES,
	normalizeLocale,
} from "./config.js";
// Locale utilities (re-exported for convenience)
export {
	commonTranslations,
	getCommonTranslations,
	getFlatTranslations,
} from "./locales/index.js";
// Types
export type {
	LanguageOption,
	LocaleConfig,
	LocalesConfig,
	MultilingualTranslations,
	NestedTranslations,
	SupportedLocale,
	TranslationDict,
} from "./types.js";
export { isSupportedLocale, SUPPORTED_LOCALES } from "./types.js";
