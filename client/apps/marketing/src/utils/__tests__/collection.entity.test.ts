import { describe, expect, it } from "vitest";
import { cleanEntityId, parseEntityId } from "../collection.entity";

describe("parseEntityId function", () => {
	it("should correctly parse an entity ID with an 'en' language prefix", () => {
		const result = parseEntityId("en/yuniel-acosta");
		expect(result).toEqual({ lang: "en", path: "yuniel-acosta" });
	});

	it("should correctly parse an entity ID with an 'es' language prefix", () => {
		const result = parseEntityId("es/yuniel-acosta");
		expect(result).toEqual({ lang: "es", path: "yuniel-acosta" });
	});

	it("should correctly parse an entity ID without a language prefix", () => {
		const result = parseEntityId("yuniel-acosta");
		expect(result).toEqual({ lang: null, path: "yuniel-acosta" });
	});

	it("should return null lang for unsupported language prefixes", () => {
		// zh-cn is not a supported locale, so it should be treated as part of the path
		const result = parseEntityId("zh-cn/yuniel-acosta");
		expect(result).toEqual({ lang: null, path: "zh-cn/yuniel-acosta" });
	});

	it("should handle entity IDs with multiple slashes", () => {
		const result = parseEntityId("en/blog/my-post");
		expect(result).toEqual({ lang: "en", path: "blog/my-post" });
	});

	it("should handle entity IDs with numbers", () => {
		const result = parseEntityId("es/article-123");
		expect(result).toEqual({ lang: "es", path: "article-123" });
	});
});

describe("cleanEntityId function", () => {
	it("should remove 'en' language prefix from entity ID", () => {
		const result = cleanEntityId("en/yuniel-acosta");
		expect(result).toBe("yuniel-acosta");
	});

	it("should remove 'es' language prefix from entity ID", () => {
		const result = cleanEntityId("es/yuniel-acosta");
		expect(result).toBe("yuniel-acosta");
	});

	it("should NOT remove unsupported language prefix from entity ID", () => {
		// zh-cn is not a supported locale, so it remains in the path
		const result = cleanEntityId("zh-cn/yuniel-acosta");
		expect(result).toBe("zh-cn/yuniel-acosta");
	});

	it("should return the original entity ID when no language prefix exists", () => {
		const result = cleanEntityId("yuniel-acosta");
		expect(result).toBe("yuniel-acosta");
	});

	it("should handle entity IDs with multiple path segments for supported locales", () => {
		const result = cleanEntityId("es/blog/tech/article");
		expect(result).toBe("blog/tech/article");
	});

	it("should NOT strip unsupported locale prefixes from multi-segment paths", () => {
		// fr is not a supported locale
		const result = cleanEntityId("fr/blog/tech/article");
		expect(result).toBe("fr/blog/tech/article");
	});
});
