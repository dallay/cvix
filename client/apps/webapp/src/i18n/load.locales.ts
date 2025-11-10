import type {
	LocaleMessage,
	LocaleMessageObject,
	LocaleMessageValue,
} from "./types";

// Utility function to flatten nested objects into a flat key structure
function flattenKeys(
	obj: Record<string, LocaleMessageValue>,
	parentKey = "",
	result: Record<string, LocaleMessageValue> = {},
): Record<string, LocaleMessageValue> {
	for (const [key, value] of Object.entries(obj)) {
		const newKey = parentKey ? `${parentKey}.${key}` : key;
		if (typeof value === "object" && value !== null && !Array.isArray(value)) {
			// Preserve top-level keys by merging the original object
			result[key] = value;
			flattenKeys(value as LocaleMessageObject, newKey, result);
		} else {
			result[newKey] = value;
		}
	}
	return result;
}

import { deepmerge } from "@loomify/utilities";

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
 * Flattens nested keys in a locale message object.
 * @param messages - The array of locale message objects to flatten.
 * @returns The array of flattened locale message objects.
 */
function flattenLocaleMessages(messages: LocaleMessage[]): LocaleMessage[] {
	return messages.map((message) => flattenKeys(message));
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
		// Use dynamic imports for lazy loading - load all locale files
		const modulePromises = [
			import(`./locales/${locale}/global.json`),
			import(`./locales/${locale}/error.json`),
			import(`./locales/${locale}/login.json`),
			import(`./locales/${locale}/register.json`),
			import(`./locales/${locale}/resume.json`),
			import(`./locales/${locale}/settings.json`),
			import(`./locales/${locale}/workspace.json`),
		];

		const modules = await Promise.all(modulePromises);
		const messages = modules
			.filter(isValidLocaleModule)
			.map((module) => module.default);

		const flattenedMessages = flattenLocaleMessages(messages);

		if (flattenedMessages.length === 0) {
			console.warn(`No locale files found for locale: ${locale}`);
			const emptyResult: LocaleMessage = {};
			localeCache.set(locale, emptyResult);
			return emptyResult;
		}

		const result = deepmerge.all(flattenedMessages) as LocaleMessage;
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

	const flattenedMessages = flattenLocaleMessages(messages);

	if (flattenedMessages.length === 0) {
		console.warn(`No locale files found for locale: ${locale}`);
		const emptyResult: LocaleMessage = {};
		localeCache.set(locale, emptyResult);
		return emptyResult;
	}

	const result = deepmerge.all(flattenedMessages) as LocaleMessage;
	localeCache.set(locale, result);
	return result;
}
