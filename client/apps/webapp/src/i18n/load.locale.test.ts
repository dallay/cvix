import { describe, expect, it, vi } from "vitest";
import { getLocaleModules, getLocaleModulesSync } from "./load.locales";

/**
 * Integration tests for locale loading.
 * These tests load actual locale files from the file system.
 */
describe("getLocaleModulesSync", () => {
	it("loads and merges English locale files correctly", () => {
		const result = getLocaleModulesSync("en");

		// Verify core structure exists
		expect(result).toBeDefined();
		expect(typeof result).toBe("object");

		// Verify key top-level keys exist (these come from different JSON files)
		expect(result).toHaveProperty("global");
		expect(result).toHaveProperty("error");
		expect(result).toHaveProperty("login");
		expect(result).toHaveProperty("register");
		expect(result).toHaveProperty("resume");
		expect(result).toHaveProperty("workspace");

		// Settings uses flat keys (settings.title, settings.description, etc.)
		expect(result).toHaveProperty("settings.title");
		expect(result).toHaveProperty("settings.description");

		// Verify some nested translations to ensure merging works
		expect(result.global).toHaveProperty("ribbon");
		expect(result.global).toHaveProperty("navigation");
		expect(result.global).toHaveProperty("common");

		expect(result.error).toHaveProperty("title");
		expect(result.error).toHaveProperty("message");

		expect(result.login).toHaveProperty("title");
		expect(result.login).toHaveProperty("form");

		expect(result.resume).toHaveProperty("title");
		expect(result.resume).toHaveProperty("fields");

		expect(result.workspace).toHaveProperty("selector");
	});

	it("loads and merges Spanish locale files correctly", () => {
		const result = getLocaleModulesSync("es");

		// Verify core structure exists
		expect(result).toBeDefined();
		expect(typeof result).toBe("object");

		// Verify key top-level keys exist
		expect(result).toHaveProperty("global");
		expect(result).toHaveProperty("error");
		expect(result).toHaveProperty("login");
		expect(result).toHaveProperty("register");
		expect(result).toHaveProperty("resume");
		expect(result).toHaveProperty("workspace");

		// Settings uses flat keys
		expect(result).toHaveProperty("settings.title");

		// Verify some translations are in Spanish
		expect(result.global).toHaveProperty("ribbon");
		expect(result.error).toHaveProperty("title");
		expect(result.login).toHaveProperty("title");
		expect(result.resume).toHaveProperty("title");
	});

	it("returns empty object for unsupported locale", () => {
		const consoleSpy = vi.spyOn(console, "warn").mockImplementation(() => {});

		const result = getLocaleModulesSync("fr");

		expect(result).toEqual({});
		expect(consoleSpy).toHaveBeenCalledWith(
			"No locale files found for locale: fr",
		);

		consoleSpy.mockRestore();
	});

	it("caches merged locale messages to avoid redundant merging", () => {
		const firstCall = getLocaleModulesSync("en");
		const secondCall = getLocaleModulesSync("en");

		// Same reference means it was cached
		expect(secondCall).toBe(firstCall);
	});
});

describe("getLocaleModules", () => {
	it("loads and merges English locale files asynchronously", async () => {
		const result = await getLocaleModules("en");

		// Verify core structure exists
		expect(result).toBeDefined();
		expect(typeof result).toBe("object");

		// Verify all locale namespaces are loaded
		expect(result).toHaveProperty("global");
		expect(result).toHaveProperty("error");
		expect(result).toHaveProperty("login");
		expect(result).toHaveProperty("register");
		expect(result).toHaveProperty("resume");
		expect(result).toHaveProperty("workspace");
		expect(result).toHaveProperty("settings.title");
	});

	it("caches async locale messages", async () => {
		const firstCall = await getLocaleModules("es");
		const secondCall = await getLocaleModules("es");

		// Same reference means it was cached
		expect(secondCall).toBe(firstCall);
	});
});
