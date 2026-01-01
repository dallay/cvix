import type { Multilingual } from "@cvix/i18n/astro";
import { describe, expect, test, vi } from "vitest";
import {
	getLocalePaths,
	localeParams,
	useTranslatedPath,
	useTranslations,
} from "../index.ts";

// Mock UI data for testing
const mockUI = {
	en: {
		hello: "Hello",
		welcome: "Welcome {name}",
		missing: "This exists in English",
		repeat: "Hello {name}, {name}!",
		complex: "Hello {name}, you are {age} years old.",
	},
	es: {
		hello: "Hola",
		welcome: "Bienvenido {name}",
		// Note: 'missing' key doesn't exist in Spanish
	},
};

// Mock the types module (used by some internal functions)
vi.mock("../types", () => ({
	DEFAULT_LOCALE: "en",
	LOCALES: { en: "English", es: "Spanish" },
	SHOW_DEFAULT_LANG_IN_URL: true, // Always true in @cvix/i18n
}));

describe("useTranslations", () => {
	test("returns translation for string key in specified language", () => {
		const t = useTranslations("es", mockUI);
		expect(t("hello")).toBe("Hola");
	});

	test("falls back to DEFAULT_LOCALE when key is missing in specified language", () => {
		const t = useTranslations("es", mockUI);
		expect(t("missing")).toBe("This exists in English");
	});

	test("returns the key itself when translation doesn't exist in any language", () => {
		const t = useTranslations("en");
		expect(t("nonexistent")).toBe("nonexistent");
	});

	test("returns correct translation for multilingual object", () => {
		const t = useTranslations("es");
		const multilingual: Multilingual = {
			en: "Hello world",
			es: "Hola mundo",
		};
		expect(t(multilingual)).toBe("Hola mundo");
	});

	test("falls back to DEFAULT_LOCALE when key is missing in multilingual object", () => {
		const t = useTranslations("es");
		const multilingual: Multilingual = {
			en: "English only",
			// No Spanish translation
		};
		expect(t(multilingual)).toBe("English only");
	});

	test("returns empty string when multilingual object has no matching language", () => {
		const t = useTranslations("es");
		// Test with empty object - no translations available
		const multilingual: Multilingual = {};
		expect(t(multilingual)).toBe("");
	});

	test("replaces variables in translation string", () => {
		const t = useTranslations("en", mockUI);
		expect(t("welcome", { name: "John" })).toBe("Welcome John");
	});

	test("replaces multiple occurrences of the same variable", () => {
		const t = useTranslations("en", mockUI);
		// No need to spy on repeat since it's already defined in the mock
		expect(t("repeat", { name: "John" })).toBe("Hello John, John!");
	});

	test("handles multiple different variables", () => {
		const t = useTranslations("en", mockUI);
		// No need to spy on complex since it's already defined in the mock
		expect(t("complex", { name: "John", age: 30 })).toBe(
			"Hello John, you are 30 years old.",
		);
	});
});

describe("useTranslatedPath", () => {
	// Note: SHOW_DEFAULT_LANG_IN_URL is always true in @cvix/i18n/astro
	// The mock of ../types doesn't affect the shared package's implementation

	test("translates a simple path to a non-default language", () => {
		const translatePath = useTranslatedPath("en");
		expect(translatePath("/about", "es")).toBe("/es/about");
	});

	test("default language always has prefix (SHOW_DEFAULT_LANG_IN_URL is always true)", () => {
		const translatePath = useTranslatedPath("en");
		expect(translatePath("/about", "en")).toBe("/en/about");
	});

	test("normalizes paths without a leading slash", () => {
		const translatePath = useTranslatedPath("en");
		expect(translatePath("about", "es")).toBe("/es/about");
	});

	test("prevents duplicate language prefixes", () => {
		const translatePath = useTranslatedPath("en");
		expect(translatePath("/en/about", "en")).toBe("/en/about");
		expect(translatePath("/es/privacy-policy", "es")).toBe(
			"/es/privacy-policy",
		);
	});

	test("uses the current language when no target language is specified", () => {
		const translatePath = useTranslatedPath("es");
		expect(translatePath("/about")).toBe("/es/about");
	});
});

describe("getLocalePaths", () => {
	// Mock getRelativeLocaleUrl from astro:i18n
	vi.mock("astro:i18n", () => ({
		getRelativeLocaleUrl: vi.fn((lang, path) => `/${lang}${path}`),
	}));

	test("returns locale paths for all configured languages", () => {
		const url = new URL("https://example.com/en/about");
		const paths = getLocalePaths(url);

		// Should have an entry for each language in LOCALES
		expect(paths.length).toBe(
			Object.keys(vi.mocked({ en: "English", es: "Spanish" })).length,
		);

		// Check structure of returned objects
		for (const path of paths) {
			expect(path).toHaveProperty("lang");
			expect(path).toHaveProperty("path");
			expect(typeof path.lang).toBe("string");
			expect(typeof path.path).toBe("string");
		}
	});

	test("correctly extracts and transforms pathname", () => {
		const url = new URL("https://example.com/en/about");
		const paths = getLocalePaths(url);

		// Find the Spanish path
		const esPath = paths.find((p) => p.lang === "es");
		expect(esPath).toBeDefined();
		expect(esPath?.path).toBe("/es/about");

		// Find the English path
		const enPath = paths.find((p) => p.lang === "en");
		expect(enPath).toBeDefined();
		expect(enPath?.path).toBe("/en/about");
	});

	test("handles URLs without language prefix", () => {
		const url = new URL("https://example.com/about");
		const paths = getLocalePaths(url);

		// Should still create paths for all languages
		expect(paths.length).toBeGreaterThan(0);

		// Check a specific language path
		const esPath = paths.find((p) => p.lang === "es");
		expect(esPath).toBeDefined();
		expect(esPath?.path).toBe("/es/about");
	});
});

describe("localeParams", () => {
	test("returns params for all configured languages", () => {
		// Should have an entry for each language in LOCALES
		expect(localeParams.length).toBe(
			Object.keys(vi.mocked({ en: "English", es: "Spanish" })).length,
		);

		// Each entry should have a params object with a lang property
		for (const param of localeParams) {
			expect(param).toHaveProperty("params");
			expect(param.params).toHaveProperty("lang");
			expect(typeof param.params.lang).toBe("string");
		}
	});

	test("includes all supported languages", () => {
		// Check that specific languages are included
		const langs = localeParams.map((param) => param.params.lang);
		expect(langs).toContain("en");
		expect(langs).toContain("es");
	});
});
