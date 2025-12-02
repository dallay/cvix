import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { __resetSiteUrlCache, getSiteUrl } from "../config";

describe("getSiteUrl", () => {
	const originalEnv = process.env;

	beforeEach(() => {
		// Reset environment variables and cache before each test
		process.env = { ...originalEnv };
		delete process.env.CF_PAGES_URL;
		delete process.env.SITE_URL;
		// Reset the memoization cache
		__resetSiteUrlCache();
	});

	afterEach(() => {
		// Restore original environment after each test
		process.env = originalEnv;
	});

	it("should return production URL when CF_PAGES_URL is not set", () => {
		const result = getSiteUrl();
		expect(result).toBe("https://cvix.pages.dev");
	});

	it("should return SITE_URL when environment variable is set", () => {
		const customUrl = "https://custom.example.com";
		process.env.SITE_URL = customUrl;

		const result = getSiteUrl();
		expect(result).toBe(customUrl);
	});

	it("should return CF_PAGES_URL when environment variable is set", () => {
		const previewUrl = "https://abc123.example.com";
		process.env.CF_PAGES_URL = previewUrl;

		const result = getSiteUrl();
		expect(result).toBe(previewUrl);
	});

	it("should prefer SITE_URL over CF_PAGES_URL", () => {
		process.env.SITE_URL = "https://priority.example.com";
		process.env.CF_PAGES_URL = "https://fallback.example.com";

		const result = getSiteUrl();
		expect(result).toBe("https://priority.example.com");
	});

	it("should handle empty CF_PAGES_URL by falling back to production", () => {
		process.env.CF_PAGES_URL = "";

		const result = getSiteUrl();
		expect(result).toBe("https://cvix.pages.dev");
	});

	it("should strip trailing slashes from URLs", () => {
		process.env.SITE_URL = "https://example.com///";

		const result = getSiteUrl();
		expect(result).toBe("https://example.com");
	});

	it("should reject invalid URLs and fall back to production", () => {
		process.env.SITE_URL = "not-a-url";
		process.env.CF_PAGES_URL = "also-invalid";

		const result = getSiteUrl();
		expect(result).toBe("https://cvix.pages.dev");
	});

	it("should handle whitespace-only values", () => {
		process.env.SITE_URL = "   ";

		const result = getSiteUrl();
		expect(result).toBe("https://cvix.pages.dev");
	});
});
