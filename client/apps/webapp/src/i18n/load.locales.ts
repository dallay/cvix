import type {
	LocaleMessage,
	LocaleMessageObject,
	LocaleMessageValue,
} from "./types";

/**
 * Convert a nested locale message object into a flat map where nested paths become dot-notated keys.
 *
 * Preserves any top-level object values under their original key while also adding flattened
 * dot-delimited entries for nested properties (e.g., `{ a: { b: "x" } }` becomes `{ a: { b: "x" }, "a.b": "x" }`).
 *
 * @param obj - The source object to flatten
 * @param parentKey - Optional prefix used for nested keys during recursion
 * @param result - Optional accumulator for flattened entries (used internally)
 * @returns A record mapping dot-notated keys to their locale message values, including preserved top-level objects
 */
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
 * Type guard that checks whether a loaded locale module exports a `default` object suitable as a locale message.
 *
 * @param module - The imported module to validate
 * @returns `true` if `module` is a non-null object with a `default` property whose value is an object, `false` otherwise.
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
 * Apply key flattening to each locale message in the input array.
 *
 * @param messages - Array of locale message objects to flatten into dot-notated key maps
 * @returns An array of LocaleMessage objects with nested keys converted to flat dot-notated keys
 */
function flattenLocaleMessages(messages: LocaleMessage[]): LocaleMessage[] {
	return messages.map((message) => flattenKeys(message));
}

/**
 * Load and merge all JSON message files for the specified locale into a single LocaleMessage.
 *
 * The merged result is cached to avoid redundant loads. If no locale files are found or an error occurs,
 * an empty LocaleMessage is cached and returned.
 *
 * @param locale - Locale code (for example, `en`, `es`)
 * @returns The merged LocaleMessage for `locale`; an empty object if no files were found or on error.
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
 * Synchronously load, merge, and cache all JSON locale message files for the specified locale.
 *
 * @param locale - Locale identifier (for example, "en" or "fr")
 * @returns The merged LocaleMessage containing flattened dot-notated keys to messages; if no locale files are found, returns and caches an empty object
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