import { createI18n } from "vue-i18n";
import { getLocaleModules, getLocaleModulesSync } from "./load.locales";

export interface Language {
	name: string;
	code: string;
}

export const LANGUAGES: ReadonlyArray<Language> = [
	{ name: "English", code: "en" },
	{ name: "Español", code: "es" },
] as const;

export const SUPPORTED_LOCALES = LANGUAGES.map(
	(lang) => lang.code,
) as readonly Language["code"][];
export type SupportedLocale = (typeof SUPPORTED_LOCALES)[number];

export const DEFAULT_LOCALE: SupportedLocale = LANGUAGES[0]?.code ?? "en";

export const LANGUAGE_STORAGE_KEY = "currentLanguage";

function getLocale(): SupportedLocale {
	// Try localStorage first
	try {
		const stored = localStorage.getItem(LANGUAGE_STORAGE_KEY);
		if (stored && SUPPORTED_LOCALES.includes(stored as SupportedLocale)) {
			return stored as SupportedLocale;
		}
	} catch (error) {
		console.error("Failed to get locale from localStorage:", error);
		// localStorage might not be available
	}

	// Try browser language detection
	try {
		if (typeof navigator !== "undefined" && navigator.language) {
			const browserLocale = navigator.language.split("-")[0];
			if (SUPPORTED_LOCALES.includes(browserLocale as SupportedLocale)) {
				return browserLocale as SupportedLocale;
			}
		}
	} catch (error) {
		console.error("Browser language detection failed:", error);
		// Browser language detection failed
	}

	return DEFAULT_LOCALE;
}

const currentLocale = getLocale();

export const i18n = createI18n({
	legacy: false,
	locale: currentLocale,
	fallbackLocale: DEFAULT_LOCALE,
	globalInjection: true,
});

i18n.global.setLocaleMessage(
	currentLocale,
	getLocaleModulesSync(currentLocale),
);

/**
 * Sets the application locale and updates i18n messages.
 * Loads and merges locale messages using getLocaleModules (deepmerge).
 * Persists the selected locale in localStorage and updates <html lang>.
 * @param locale SupportedLocale
 */
export async function setLocale(locale: SupportedLocale) {
	const targetLocale = SUPPORTED_LOCALES.includes(locale)
		? locale
		: (() => {
				return DEFAULT_LOCALE;
			})();

	if (!i18n.global.availableLocales.includes(targetLocale)) {
		i18n.global.setLocaleMessage(
			targetLocale,
			await getLocaleModules(targetLocale),
		);
	}

	// Set the locale - in composition API mode, locale is a WritableComputedRef
	i18n.global.locale.value = targetLocale;

	try {
		localStorage.setItem(LANGUAGE_STORAGE_KEY, targetLocale);
	} catch (error) {
		console.error("Failed to save locale to localStorage:", error);
		// Failed to save locale to localStorage
	}

	document.documentElement.lang = targetLocale;
}
