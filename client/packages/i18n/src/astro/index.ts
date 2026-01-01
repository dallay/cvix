/**
 * @cvix/i18n/astro - Astro-specific i18n Utilities
 *
 * Functions for internationalization in Astro applications.
 * These utilities are designed to work with Astro's routing and i18n features.
 *
 * @example
 * ```typescript
 * import { useTranslations, useTranslatedPath } from '@cvix/i18n/astro';
 *
 * const t = useTranslations('en');
 * const translatePath = useTranslatedPath('en');
 *
 * t('greeting'); // "Hello"
 * translatePath('/about', 'es'); // "/es/about"
 * ```
 */

import { DEFAULT_LOCALE, SUPPORTED_LOCALES } from "../config";
import type {
	Lang,
	LocalePath,
	Multilingual,
	TranslatePathFunction,
	TranslationFunction,
	UIMultilingual,
} from "./types";

/**
 * Configuration to determine if default language should be shown in URLs.
 * When false, the default language won't have a prefix in URLs.
 *
 * @example
 * If false: /about (for default language) and /es/about (for other languages)
 * If true: /en/about and /es/about
 *
 * @default true
 */
export const SHOW_DEFAULT_LANG_IN_URL = true;

/**
 * Creates a translation function for a specific language.
 *
 * Supports both:
 * - String keys that look up translations in a UI dictionary
 * - Multilingual objects that contain translations for multiple languages
 *
 * @param lang - The current language code
 * @param uiDict - Optional UI dictionary with translations
 * @returns A function that translates keys or multilingual objects
 *
 * @example
 * ```typescript
 * const ui: UIMultilingual = {
 *   en: { greeting: 'Hello, {name}!' },
 *   es: { greeting: 'Hola, {name}!' }
 * };
 *
 * const t = useTranslations('en', ui);
 * t('greeting', { name: 'World' }); // "Hello, World!"
 *
 * // With multilingual object:
 * t({ en: 'English text', es: 'Texto en español' }); // "English text"
 * ```
 */
export function useTranslations(
	lang: Lang,
	uiDict: UIMultilingual | undefined = { en: {}, es: {} },
): TranslationFunction {
	const ui = uiDict ?? { en: {}, es: {} };
	return function t(
		multilingualOrKey: Multilingual | string,
		variables?: Record<string, string | number>,
	): string {
		let text: string;

		if (multilingualOrKey === undefined) {
			throw new Error(
				"t(): UNDEFINED was passed as the translation key or multilingual object.",
			);
		}

		if (typeof multilingualOrKey === "string") {
			// When it's a string, look in ui[lang] or ui[DEFAULT_LOCALE]
			const langUI = ui[lang] || {};
			const defaultUI = ui[DEFAULT_LOCALE as Lang] || {};

			text =
				langUI[multilingualOrKey] ??
				defaultUI[multilingualOrKey] ??
				multilingualOrKey;
		} else {
			// When it's a TextMultilingual object, return the value for lang or DEFAULT_LOCALE
			text = multilingualOrKey[lang] ?? multilingualOrKey[DEFAULT_LOCALE] ?? "";
		}

		// Replace variables in the translation string if provided
		if (variables) {
			return Object.entries(variables).reduce((result, [key, value]) => {
				// Use a global regex to replace all occurrences
				return result.replace(new RegExp(`\\{${key}\\}`, "g"), String(value));
			}, text);
		}

		return text;
	};
}

/**
 * Creates a path translation function for routing between languages.
 *
 * @param lang - The current language code
 * @returns A function that translates paths to target languages
 *
 * @example
 * ```typescript
 * const translatePath = useTranslatedPath('en');
 *
 * translatePath('/about'); // '/about' if en is default and SHOW_DEFAULT_LANG_IN_URL is false
 * translatePath('/about', 'es'); // '/es/about'
 * translatePath('/en/about', 'en'); // '/en/about' (prevents duplicate prefixes)
 * ```
 */
export function useTranslatedPath(lang: Lang): TranslatePathFunction {
	return function translatePath(path: string, targetLang: Lang = lang): string {
		// Don't modify external URLs (http://, https://, //, mailto:, tel:, etc.)
		if (/^(https?:\/\/|\/\/|mailto:|tel:)/.test(path)) {
			return path;
		}

		// Ensure path starts with a slash
		const normalizedPath = path.startsWith("/") ? path : `/${path}`;

		// Check if path already starts with the target language prefix
		const langPrefixRegex = new RegExp(`^/${targetLang}/`);
		if (langPrefixRegex.test(normalizedPath)) {
			// Path already has the language prefix, return as is
			return normalizedPath;
		}

		// For default language, we might not show the language prefix based on config
		if (!SHOW_DEFAULT_LANG_IN_URL && targetLang === DEFAULT_LOCALE) {
			return normalizedPath;
		}

		// For other languages, or if we always show the language prefix
		return `/${targetLang}${normalizedPath}`;
	};
}

/**
 * Generates an array of locale paths for different languages based on a given URL.
 * Useful for language switcher components.
 *
 * Note: This function does NOT use Astro's getRelativeLocaleUrl to remain
 * framework-independent. Use getLocalePathsWithAstro for Astro-specific behavior.
 *
 * @param url - The URL to extract and transform the pathname
 * @returns An array of LocalePath objects containing language code and localized path
 *
 * @example
 * ```typescript
 * // For URL: new URL('https://example.com/en/about')
 * getLocalePaths(new URL('https://example.com/en/about'));
 * // Returns: [
 * //   { lang: 'en', path: '/en/about' },
 * //   { lang: 'es', path: '/es/about' }
 * // ]
 * ```
 */
export function getLocalePaths(url: URL): LocalePath[] {
	// Create a regex pattern that matches only language prefixes
	const langPrefixPattern = `^\\/(${SUPPORTED_LOCALES.join("|")})(?:\\/|$)`;
	const langPrefixRegex = new RegExp(langPrefixPattern);

	// Extract the pathname without the language prefix if it exists
	const pathWithoutLangPrefix = url.pathname.replace(langPrefixRegex, "/");

	// If pathWithoutLangPrefix is just "/", keep it as "/"
	const cleanPath =
		pathWithoutLangPrefix === "/"
			? ""
			: pathWithoutLangPrefix.replace(/^\//, "");

	return SUPPORTED_LOCALES.map((lang) => ({
		lang,
		path: `/${lang}${cleanPath ? `/${cleanPath}` : ""}`.replace(/\/+/g, "/"),
	}));
}

/**
 * Helper to get locale params for Astro's `getStaticPaths` function.
 *
 * @returns Array of objects with params for each locale
 *
 * @example
 * ```typescript
 * // In an Astro page:
 * export function getStaticPaths() {
 *   return localeParams;
 * }
 * // Returns: [{ params: { lang: 'en' } }, { params: { lang: 'es' } }]
 * ```
 */
export const localeParams = SUPPORTED_LOCALES.map((lang) => ({
	params: { lang },
}));

/**
 * Retrieves a localized string using the current environment language.
 * Useful for server-side rendering contexts.
 *
 * @param key - Translation key or multilingual object
 * @param ui - Optional UI dictionary with translations
 * @returns The translated string
 *
 * @example
 * ```typescript
 * const title = retrieveLocalizedString('page.title', ui);
 * // Or with multilingual:
 * const title = retrieveLocalizedString({ en: 'Hello', es: 'Hola' });
 * ```
 */
export function retrieveLocalizedString(
	key: string | Multilingual,
	ui?: UIMultilingual,
): string {
	// Try to get language from environment, fallback to DEFAULT_LOCALE
	const currentLang =
		(typeof import.meta !== "undefined" &&
			(import.meta as unknown as { env?: { LANG?: string } }).env?.LANG) ||
		DEFAULT_LOCALE;
	const t = useTranslations(currentLang as Lang, ui);
	return t(key);
}

/**
 * Creates an empty UI multilingual object with entries for all supported locales.
 * Useful for initializing translation dictionaries.
 *
 * @returns An empty UIMultilingual object
 */
export function createEmptyUIMultilingual(): UIMultilingual {
	return SUPPORTED_LOCALES.reduce((acc, lang) => {
		acc[lang] = {};
		return acc;
	}, {} as UIMultilingual);
}

/**
 * Merges multiple translation modules into a single UIMultilingual object.
 * Useful for aggregating translations from different files.
 *
 * @param modules - Array of translation modules to merge
 * @returns Merged UIMultilingual object
 *
 * @example
 * ```typescript
 * const headerTranslations = { en: { title: 'Header' }, es: { title: 'Encabezado' } };
 * const footerTranslations = { en: { copyright: '2024' }, es: { copyright: '2024' } };
 *
 * const ui = mergeTranslationModules([headerTranslations, footerTranslations]);
 * // ui.en = { title: 'Header', copyright: '2024' }
 * // ui.es = { title: 'Encabezado', copyright: '2024' }
 * ```
 */
export function mergeTranslationModules(
	modules: Array<Partial<UIMultilingual>>,
): UIMultilingual {
	const result = createEmptyUIMultilingual();

	for (const module of modules) {
		for (const lang of SUPPORTED_LOCALES) {
			if (module[lang]) {
				result[lang] = {
					...result[lang],
					...module[lang],
				};
			}
		}
	}

	return result;
}

// Re-export config values commonly used in Astro apps
export { DEFAULT_LOCALE, LOCALES, SUPPORTED_LOCALES } from "../config";
// Re-export types for convenience
export type {
	AstroLocaleConfig,
	AstroLocalesConfig,
	Lang,
	LocalePath,
	Multilingual,
	TranslatePathFunction,
	TranslationFunction,
	Translations,
	UIDict,
	UIMultilingual,
} from "./types";
