import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { getSiteUrl } from "../config";

describe("getSiteUrl", () => {
	const originalEnv = process.env;

	beforeEach(() => {
		// Reset environment variables before each test
		process.env = { ...originalEnv };
		delete process.env.CF_PAGES_URL;
	});

	afterEach(() => {
		// Restore original environment after each test
		process.env = originalEnv;
	});

	it("should return production URL when CF_PAGES_URL is not set", () => {
		const result = getSiteUrl();
		expect(result).toBe("https://cvix.pages.dev");
	});

	it("should return CF_PAGES_URL when environment variable is set", () => {
		const previewUrl = "https://abc123.example.com";
		process.env.CF_PAGES_URL = previewUrl;

		const result = getSiteUrl();
		expect(result).toBe(previewUrl);
	});

	it("should handle preview deployment URLs correctly", () => {
		const previewUrl = "https://1a2b3c4d.example.com";
		process.env.CF_PAGES_URL = previewUrl;

		const result = getSiteUrl();
		expect(result).toBe(previewUrl);
	});

	it("should handle empty CF_PAGES_URL by falling back to production", () => {
		process.env.CF_PAGES_URL = "";

		const result = getSiteUrl();
		expect(result).toBe("https://cvix.pages.dev");
	});
});
