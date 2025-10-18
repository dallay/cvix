import { deepmerge } from "@loomify/utilities";
import type { LocaleMessage } from "./types";

/**
 * In-memory cache for merged locale message objects.
 * Prevents redundant file system reads and merges for previously loaded locales.
 */
const localeCache = new Map<string, LocaleMessage>();

/**
 * Type guard to ensure module has the expected structure
 */
function isValidLocaleModule(
	module: unknown,
): module is { default: LocaleMessage } {
	return (
		typeof module === "object" &&
		module !== null &&
		"default" in module &&
		typeof (module as { default: unknown }).default === "object"
	);
}

/**
 * Loads and merges all JSON message files for a given locale.
 * Uses lazy loading for better performance and caching to avoid redundant operations.
 *
 * @param locale - The locale code (e.g., 'en', 'es').
 * @returns Promise that resolves to the merged locale messages object.
 */
export async function getLocaleModules(locale: string): Promise<LocaleMessage> {
	// Check cache first
	const cached = localeCache.get(locale);
	if (cached) return cached;

	try {
		// Use dynamic imports for lazy loading
		const modulePromises = [
			import(`./locales/${locale}/global.json`),
			import(`./locales/${locale}/error.json`),
			import(`./locales/${locale}/login.json`),
			import(`./locales/${locale}/register.json`),
		];

		const modules = await Promise.all(modulePromises);
		const messages = modules
			.filter(isValidLocaleModule)
			.map((module) => module.default);

		if (messages.length === 0) {
			console.warn(`No locale files found for locale: ${locale}`);
			const emptyResult: LocaleMessage = {};
			localeCache.set(locale, emptyResult);
			return emptyResult;
		}

		const result = deepmerge.all(messages) as LocaleMessage;
		localeCache.set(locale, result);
		return result;
	} catch (error) {
		console.error(`Failed to load locale files for ${locale}:`, error);
		const emptyResult: LocaleMessage = {};
		localeCache.set(locale, emptyResult);
		return emptyResult;
	}
}

/**
 * Synchronous version for initial locale loading.
 * Falls back to eager loading for the initial setup.
 */
export function getLocaleModulesSync(locale: string): LocaleMessage {
	// Check cache first
	const cached = localeCache.get(locale);
	if (cached) return cached;

	const modules = import.meta.glob("./locales/**/*.json", { eager: true });
	const localePattern = `/locales/${locale}/`;

	const messages: LocaleMessage[] = [];

	for (const [path, module] of Object.entries(modules)) {
		if (path.includes(localePattern) && isValidLocaleModule(module)) {
			messages.push(module.default);
		}
	}

	if (messages.length === 0) {
		console.warn(`No locale files found for locale: ${locale}`);
		const emptyResult: LocaleMessage = {};
		localeCache.set(locale, emptyResult);
		return emptyResult;
	}

	const result = deepmerge.all(messages) as LocaleMessage;
	localeCache.set(locale, result);
	return result;
}
