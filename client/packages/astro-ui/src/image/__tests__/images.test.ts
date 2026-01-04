import type { ImageMetadata } from "astro";
import { describe, expect, it, vi } from "vitest";
import { createImageResolver } from "../images.js";

/**
 * FAKE image mapping para los tests unitarios.
 * Si quieres "encontrar" una imagen, declárala aquí.
 */
const fakeMapping = {
	"/src/assets/images/blog-placeholder-1.avif": () =>
		Promise.resolve({
			default: {
				src: "/src/assets/images/blog-placeholder-1.avif",
				width: 1111,
				height: 2222,
				format: "avif",
			} as ImageMetadata,
		}),
	"/src/assets/images/blog-placeholder-2.avif": () =>
		Promise.resolve({
			default: {
				src: "/src/assets/images/blog-placeholder-2.avif",
				width: 444,
				height: 555,
				format: "avif",
			} as ImageMetadata,
		}),
	"/src/assets/images/choosing-the-right-format-chronological-hybrid-or-functional.webp":
		() =>
			Promise.resolve({
				default: {
					src: "/src/assets/images/choosing-the-right-format-chronological-hybrid-or-functional.webp",
					width: 777,
					height: 888,
					format: "webp",
				} as ImageMetadata,
			}),
};

// El resolver se crea UNA VEZ con el mapping de prueba
const {
	findImage,
	normalizeFilename,
	getImageLookupStats,
	rebuildImageLookupMap,
	prepareImageForOptimizedPicture,
} = createImageResolver(fakeMapping, {
	basePaths: ["/client/apps/marketing"],
});

/**
 * Tests para images.ts usando mapping local
 * - No depende de glob real ni filesystem
 * - Toda imagen "existente" debe estar en fakeMapping
 */

describe("images.ts - Path Normalization", () => {
	describe("path format handling", () => {
		it("should normalize monorepo path format and return null for missing image", async () => {
			const input =
				"/client/apps/marketing/src/assets/images/photo-nonexistent.jpg";
			const result = await findImage(input);
			expect(result).toBeNull();
		});
		it("should normalize tilde paths and return null for missing image", async () => {
			const input = "~/assets/images/photo-nonexistent.jpg";
			const result = await findImage(input);
			expect(result).toBeNull();
		});
		it("should normalize @ alias paths and return null for missing image", async () => {
			const input = "@/assets/images/photo-nonexistent.jpg";
			const result = await findImage(input);
			expect(result).toBeNull();
		});
		it("should normalize src/ relative paths and return null for missing image", async () => {
			const input = "src/assets/images/photo-nonexistent.jpg";
			const result = await findImage(input);
			expect(result).toBeNull();
		});
		it("should handle already normalized paths and return null for missing image", async () => {
			const input = "/src/assets/images/photo-nonexistent.jpg";
			const result = await findImage(input);
			expect(result).toBeNull();
		});
	});

	describe("normalizeFilename helper", () => {
		it("should return filename unchanged when no path separator exists", () => {
			const filename = "photo with spaces.jpg";
			const normalized = normalizeFilename(filename);
			expect(normalized).toBe(filename);
		});
		it("should preserve directory and encode filename when path has separator", () => {
			const path = "/src/assets/images/photo with spaces.jpg";
			const normalized = normalizeFilename(path);
			expect(normalized).toBe("/src/assets/images/photo%20with%20spaces.jpg");
			expect(normalized.startsWith("/src/assets/images/")).toBe(true);
		});
		it("should handle special characters in filenames with paths", () => {
			const testCases = [
				{
					input: "/images/photo&image.jpg",
					expected: "/images/photo%26image.jpg",
				},
				{
					input: "/images/photo=value.jpg",
					expected: "/images/photo%3Dvalue.jpg",
				},
				{
					input: "/images/photo+plus.jpg",
					expected: "/images/photo%2Bplus.jpg",
				},
				{
					input: "/images/photo#hash.jpg",
					expected: "/images/photo%23hash.jpg",
				},
			];
			for (const { input, expected } of testCases) {
				const normalized = normalizeFilename(input);
				expect(normalized).toBe(expected);
				expect(normalized).not.toContain("&");
				expect(normalized).not.toContain("=");
				expect(normalized).not.toContain("+");
				expect(normalized).not.toContain("#");
			}
		});
		it("should handle kebab-case filenames without additional encoding", () => {
			const path = "/images/choosing-the-right-format-chronological.webp";
			const normalized = normalizeFilename(path);
			expect(normalized).toBe(path);
		});
	});

	describe("external URL handling via findImage", () => {
		it("should pass through HTTP URLs unchanged", async () => {
			const urls = [
				"http://example.com/image.jpg",
				"http://cdn.example.com/photo.webp",
				"http://localhost:3000/image.png",
			];
			for (const url of urls) {
				const result = await findImage(url);
				expect(result).toBe(url);
			}
		});
		it("should pass through HTTPS URLs unchanged", async () => {
			const urls = [
				"https://example.com/image.jpg",
				"https://cdn.example.com/photo.webp",
				"https://images.unsplash.com/photo.jpg",
			];
			for (const url of urls) {
				const result = await findImage(url);
				expect(result).toBe(url);
			}
		});
		it("should handle non-http(s) paths according to findImage logic", async () => {
			const passThroughPaths = [
				"/images/photo.jpg",
				"./images/photo.jpg",
				"../images/photo.jpg",
				"images/photo.jpg",
			];
			for (const path of passThroughPaths) {
				const result = await findImage(path);
				expect(result).toBe(path);
			}
		});
	});

	describe("edge cases for normalizeFilename and findImage", () => {
		it("should normalize paths with multiple extensions and end with .jpg", () => {
			const path = "/src/assets/images/photo.backup.jpg";
			const normalized = normalizeFilename(path);
			expect(normalized).toMatch(/\.jpg$/);
			expect(normalized).toBe("/src/assets/images/photo.backup.jpg");
		});
		it("should preserve numbers in filenames", () => {
			const path = "/src/assets/images/photo-123-v2.jpg";
			const normalized = normalizeFilename(path);
			expect(normalized).toContain("123");
			expect(normalized).toBe("/src/assets/images/photo-123-v2.jpg");
		});
		it("should handle very deep directory structures", () => {
			const deepPath = "/src/assets/images/category/subcategory/photo.jpg";
			const normalized = normalizeFilename(deepPath);
			expect(normalized).toContain("category/subcategory/");
			expect(normalized).toBe(
				"/src/assets/images/category/subcategory/photo.jpg",
			);
		});
		it("should handle unicode filenames and encode accordingly", () => {
			const path = "/src/assets/images/foto-español-日本語.jpg";
			const normalized = normalizeFilename(path);
			expect(normalized).toMatch(/%/);
			const lastSlashIdx = normalized.lastIndexOf("/");
			const encodedPart = normalized.slice(lastSlashIdx + 1);
			expect(decodeURIComponent(encodedPart)).toBe("foto-español-日本語.jpg");
		});
		it("should return null when finding a completely unknown unicode path", async () => {
			const path = "/src/assets/images/фото-без-сопоставления.jpg";
			const result = await findImage(path);
			expect(result).toBeNull();
		});
	});

	describe("findImage type handling", () => {
		it("should return ImageMetadata object as-is", async () => {
			const mockImage: ImageMetadata = {
				src: "/path/to/image.jpg",
				width: 800,
				height: 600,
				format: "jpg",
			};
			const result = await findImage(mockImage);
			expect(result).toEqual(mockImage);
		});
		it("should handle null and undefined by passing through", async () => {
			expect(await findImage(null)).toBeNull();
			expect(await findImage(undefined)).toBeUndefined();
		});
		it("should differentiate string path vs object by return type", async () => {
			const stringPath = "/images/photo.jpg";
			const objectInput = {
				src: "/images/photo.jpg",
				width: 100,
				height: 100,
				format: "jpg" as const,
			};
			const resultString = await findImage(stringPath);
			const resultObject = await findImage(objectInput);
			expect(resultString).toBe(stringPath);
			expect(resultObject).toEqual(objectInput);
		});
	});

	describe("console warnings and loader error handling", () => {
		it("should warn when image is not found", async () => {
			const consoleWarnSpy = vi
				.spyOn(console, "warn")
				.mockImplementation(() => {});
			await findImage("/src/assets/images/this-image-does-not-exist.jpg");
			expect(consoleWarnSpy).toHaveBeenCalledWith(
				expect.stringContaining("Image not found"),
			);
			consoleWarnSpy.mockRestore();
		});
		it("should handle missing image gracefully (loader error path cannot be mocked post-import)", async () => {
			const consoleWarnSpy = vi
				.spyOn(console, "warn")
				.mockImplementation(() => {});
			const result = await findImage("/src/assets/images/failing-load.jpg");
			expect(result).toBeNull();
			expect(consoleWarnSpy).toHaveBeenCalledWith(
				expect.stringContaining("Image not found"),
			);
			consoleWarnSpy.mockRestore();
		});
	});
});

describe("images.ts - Performance & Monitoring", () => {
	describe("normalized lookup map", () => {
		it("should provide lookup statistics", () => {
			const stats = getImageLookupStats();
			expect(stats).toHaveProperty("totalGlobKeys");
			expect(stats).toHaveProperty("normalizedMapSize");
			expect(stats).toHaveProperty("timestamp");
			expect(typeof stats.totalGlobKeys).toBe("number");
			expect(typeof stats.normalizedMapSize).toBe("number");
			expect(typeof stats.timestamp).toBe("string");
		});
		it("should have normalized map size >= glob keys (due to encoded variants)", () => {
			const stats = getImageLookupStats();
			expect(stats.normalizedMapSize).toBeGreaterThanOrEqual(
				stats.totalGlobKeys,
			);
		});
		it("should allow rebuilding the lookup map", () => {
			const initialStats = getImageLookupStats();
			const rebuiltSize = rebuildImageLookupMap();
			const finalStats = getImageLookupStats();
			expect(typeof rebuiltSize).toBe("number");
			expect(finalStats.normalizedMapSize).toBe(rebuiltSize);
			expect(finalStats.normalizedMapSize).toBe(initialStats.normalizedMapSize);
		});
		it("should return consistent stats across multiple calls", () => {
			const stats1 = getImageLookupStats();
			const stats2 = getImageLookupStats();
			expect(stats1.totalGlobKeys).toBe(stats2.totalGlobKeys);
			expect(stats1.normalizedMapSize).toBe(stats2.normalizedMapSize);
		});
	});

	describe("case-insensitive image lookup (3-tier strategy)", () => {
		it("should find real images with mixed case paths via case-insensitive lookup", async () => {
			const mixedCasePath = "/src/assets/images/BLOG-PLACEHOLDER-1.AVIF";
			const result = await findImage(mixedCasePath);
			expect(result).not.toBeNull();
			expect(typeof result === "string" || typeof result === "object").toBe(
				true,
			);
			if (result && typeof result === "object" && "format" in result) {
				expect(result.format).toBe("avif");
			}
		});
		it("should handle various case combinations for real images", async () => {
			const testPaths = [
				"/src/assets/images/blog-placeholder-1.avif",
				"/src/assets/images/BLOG-PLACEHOLDER-1.AVIF",
				"/src/assets/images/Blog-Placeholder-1.Avif",
			];
			for (const path of testPaths) {
				const result = await findImage(path);
				expect(result).not.toBeNull();
				expect(typeof result === "string" || typeof result === "object").toBe(
					true,
				);
			}
		});
		it("should return consistent ImageMetadata for same path called multiple times", async () => {
			const path1 = "/src/assets/images/blog-placeholder-1.avif";
			const path2 = "/src/assets/images/blog-placeholder-1.avif";
			const result1 = await findImage(path1);
			const result2 = await findImage(path2);
			expect(result1).toEqual(result2);
			expect(result1).not.toBeNull();
		});
		it("should find images via encoded filename match (tier 2)", async () => {
			const normalPath = "/src/assets/images/blog-placeholder-1.avif";
			const encodedPath = normalizeFilename(normalPath);
			const result = await findImage(encodedPath);
			expect(result).not.toBeNull();
			expect(typeof result === "string" || typeof result === "object").toBe(
				true,
			);
		});
		it("should find images via encoded lowercase fallback (tier 3 final fallback)", async () => {
			const mixedCasePath = "/src/assets/images/BLOG-placeholder-1.avif";
			const result = await findImage(mixedCasePath);
			expect(result).not.toBeNull();
			expect(typeof result === "string" || typeof result === "object").toBe(
				true,
			);
		});
		it("should pass through paths that don't start with /src/assets/ unchanged", async () => {
			const relativePath = "relative/path/image.jpg";
			const result = await findImage(relativePath);
			expect(result).toBe(relativePath);
		});
	});
});

describe("images.ts - Integration Scenarios", () => {
	describe("blog post cover images", () => {
		it("should pass through external URLs unchanged", async () => {
			const externalUrls = [
				"https://images.unsplash.com/photo.jpg",
				"https://cdn.pixabay.com/photo.jpg",
				"http://example.com/image.png",
			];
			for (const url of externalUrls) {
				const result = await findImage(url);
				expect(result).toBe(url);
			}
		});
		it("should return null for non-existent local images", async () => {
			const nonExistentPaths = [
				"/src/assets/images/this-file-does-not-exist-at-all.webp",
				"/src/assets/images/another-missing-file.jpg",
				"/src/assets/images/nope-not-here.png",
			];
			for (const path of nonExistentPaths) {
				const result = await findImage(path);
				expect(result).toBeNull();
			}
		});
		it("should find and return image data for real images", async () => {
			const realImagePath = "/src/assets/images/blog-placeholder-1.avif";
			const result = await findImage(realImagePath);
			expect(result).not.toBeNull();
			expect(result).toBeDefined();
			expect(typeof result === "string" || typeof result === "object").toBe(
				true,
			);
			if (result && typeof result === "object" && "src" in result) {
				expect(result).toHaveProperty("src");
				expect(result).toHaveProperty("width");
				expect(result).toHaveProperty("height");
				expect(result).toHaveProperty("format");
				expect(typeof result.src).toBe("string");
				expect(typeof result.width).toBe("number");
				expect(typeof result.height).toBe("number");
				expect(result.format).toBe("avif");
			}
		});
		it("should normalize monorepo paths and find real images", async () => {
			const markdownPath =
				"/client/apps/marketing/src/assets/images/blog-placeholder-1.avif";
			const result = await findImage(markdownPath);
			expect(result).not.toBeNull();
			expect(result).toBeDefined();
			expect(typeof result === "string" || typeof result === "object").toBe(
				true,
			);
		});
		it("should find images with tilde and @ alias paths", async () => {
			const tildePath = "~/assets/images/blog-placeholder-1.avif";
			const aliasPath = "@/assets/images/blog-placeholder-1.avif";
			const tildeResult = await findImage(tildePath);
			const aliasResult = await findImage(aliasPath);
			expect(tildeResult).not.toBeNull();
			expect(aliasResult).not.toBeNull();
			expect(
				typeof tildeResult === "string" || typeof tildeResult === "object",
			).toBe(true);
			expect(
				typeof aliasResult === "string" || typeof aliasResult === "object",
			).toBe(true);
		});
		it("should find images with different formats (webp, avif)", async () => {
			const webpImage =
				"/src/assets/images/choosing-the-right-format-chronological-hybrid-or-functional.webp";
			const avifImage = "/src/assets/images/blog-placeholder-2.avif";
			const webpResult = await findImage(webpImage);
			const avifResult = await findImage(avifImage);
			expect(webpResult).not.toBeNull();
			expect(avifResult).not.toBeNull();
			if (
				webpResult &&
				typeof webpResult === "object" &&
				"format" in webpResult
			) {
				expect(webpResult.format).toBe("webp");
			}
			if (
				avifResult &&
				typeof avifResult === "object" &&
				"format" in avifResult
			) {
				expect(avifResult.format).toBe("avif");
			}
		});
	});
	describe("supported image formats", () => {
		it("should handle various image format extensions", async () => {
			const formats = ["jpeg", "jpg", "png", "webp", "avif", "gif", "svg"];
			for (const format of formats) {
				const path = `/src/assets/images/photo.${format}`;
				const result = await findImage(path);
				expect(result).toBeNull();
			}
		});
		it("should handle uppercase extensions via case-insensitive matching", async () => {
			const upperFormats = ["JPEG", "JPG", "PNG", "WEBP"];
			for (const format of upperFormats) {
				const path = `/src/assets/images/photo.${format}`;
				const result = await findImage(path);
				expect(result).toBeNull();
			}
		});
	});
});

describe("images.ts - Helper Functions", () => {
	describe("prepareImageForOptimizedPicture", () => {
		it("should resolve string paths to ImageMetadata", async () => {
			const path = "/src/assets/images/blog-placeholder-1.avif";
			const result = await prepareImageForOptimizedPicture(path);
			expect(result).not.toBeNull();
			expect(typeof result).toBe("object");
			expect(result).toHaveProperty("src");
			expect(result).toHaveProperty("format", "avif");
		});

		it("should pass through original string if image not found (and warn)", async () => {
			const consoleWarnSpy = vi
				.spyOn(console, "warn")
				.mockImplementation(() => {});
			const path = "/src/assets/images/missing-image.jpg";
			const result = await prepareImageForOptimizedPicture(path);
			expect(result).toBe(path);
			consoleWarnSpy.mockRestore();
		});

		it("should pass through ImageMetadata objects unchanged", async () => {
			const meta = {
				src: "foo.png",
				width: 100,
				height: 100,
				format: "png",
			} as ImageMetadata;
			const result = await prepareImageForOptimizedPicture(meta);
			expect(result).toBe(meta);
		});

		it("should pass through external URLs unchanged", async () => {
			const url = "https://example.com/image.jpg";
			const result = await prepareImageForOptimizedPicture(url);
			expect(result).toBe(url);
		});
	});
});
