/**
 * Marketing i18n Functions
 *
 * This file provides i18n utilities for the marketing app.
 * Core functions are re-exported from @cvix/i18n/astro,
 * while Astro-specific functions (using astro:i18n) remain local.
 */
import { getRelativeLocaleUrl } from "astro:i18n";
import {
	localeParams as baseLocaleParams,
	useTranslatedPath as baseUseTranslatedPath,
	useTranslations as baseUseTranslations,
	type Lang,
	type LocalePath,
	SUPPORTED_LOCALES,
} from "@cvix/i18n/astro";

export type { Lang } from "@cvix/i18n/astro";

import { ui } from "./ui";

/**
 * Helper to get the translation function with marketing UI translations
 * @param lang - The current language
 * @returns - The translation function
 */
export function useTranslations(lang: Lang) {
	return baseUseTranslations(lang, ui);
}

/**
 * Helper to translate paths between languages
 * Re-exported from @cvix/i18n/astro
 *
 * @param lang - The current language
 * @returns - A function that translates paths
 *
 * @example
 * const translatePath = useTranslatedPath('en');
 * translatePath('/about'); // returns '/about' if en is default and SHOW_DEFAULT_LANG_IN_URL is false
 * translatePath('/about', 'es'); // returns '/es/about'
 * translatePath('/en/about', 'en'); // returns '/en/about' (prevents duplicate prefixes)
 */
export const useTranslatedPath = baseUseTranslatedPath;

/**
 * Generates an array of locale paths for different languages based on a given URL.
 * Uses Astro's getRelativeLocaleUrl for proper routing integration.
 *
 * @param url - The URL to extract and transform the pathname
 * @returns An array of LocalePath objects containing language code and localized path
 *
 * @example
 * // For URL: new URL('https://example.com/en/about')
 * // Returns: [
 * //   { lang: 'en', path: '/en/about' },
 * //   { lang: 'es', path: '/es/about' },
 * //   ...
 * // ]
 */
export function getLocalePaths(url: URL): LocalePath[] {
	// Create a regex pattern that matches only language prefixes
	const langPrefixPattern = `^\\/(${SUPPORTED_LOCALES.join("|")})`;
	const langPrefixRegex = new RegExp(langPrefixPattern);

	// Extract the pathname without the language prefix if it exists
	const pathWithoutLangPrefix = url.pathname.replace(langPrefixRegex, "");

	// If pathWithoutLangPrefix is empty, it means the URL was just a language prefix
	// In that case, use "/" as the path
	const cleanPath = pathWithoutLangPrefix || "/";

	return SUPPORTED_LOCALES.map((lang) => {
		return {
			lang,
			path: getRelativeLocaleUrl(lang, cleanPath),
		};
	});
}

/**
 * Helper to get locale params for Astro's `getStaticPaths` function
 * @returns - The list of locale params
 * @see https://docs.astro.build/en/guides/routing/#dynamic-routes
 */
export const localeParams = baseLocaleParams;
