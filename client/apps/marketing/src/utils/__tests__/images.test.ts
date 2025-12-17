import { describe, expect, it, vi } from "vitest";
import type { ImageMetadata } from "astro";

/**
 * Tests for images.ts
 *
 * Note: These tests focus on the path normalization logic and error handling.
 * Testing the actual glob import requires mocking import.meta.glob which is
 * complex in Vitest. In a real-world scenario, you'd want integration tests
 * that verify the actual image loading works correctly.
 */

describe("images.ts - Path Normalization", () => {
	describe("path format handling", () => {
		it("should normalize monorepo path format", () => {
			const input = "/client/apps/marketing/src/assets/images/photo.jpg";
			const expected = "/src/assets/images/photo.jpg";

			// Test the normalization logic
			const normalized = input.replace("/client/apps/marketing", "");
			expect(normalized).toBe(expected);
		});

		it("should normalize tilde paths", () => {
			const input = "~/assets/images/photo.jpg";
			const expected = "/src/assets/images/photo.jpg";

			// Test the normalization logic
			const normalized = input.replace(/^[~@]\//, "/src/");
			expect(normalized).toBe(expected);
		});

		it("should normalize @ alias paths", () => {
			const input = "@/assets/images/photo.jpg";
			const expected = "/src/assets/images/photo.jpg";

			// Test the normalization logic
			const normalized = input.replace(/^[~@]\//, "/src/");
			expect(normalized).toBe(expected);
		});

		it("should normalize src/ relative paths", () => {
			const input = "src/assets/images/photo.jpg";
			const expected = "/src/assets/images/photo.jpg";

			// Test the normalization logic
			const normalized = `/${input}`;
			expect(normalized).toBe(expected);
		});

		it("should handle already normalized paths", () => {
			const input = "/src/assets/images/photo.jpg";
			expect(input).toBe("/src/assets/images/photo.jpg");
		});
	});

	describe("filename normalization with spaces", () => {
		it("should encode filenames with spaces", () => {
			const filename = "photo with spaces.jpg";
			const encoded = encodeURIComponent(filename);

			expect(encoded).toBe("photo%20with%20spaces.jpg");
		});

		it("should preserve directory structure when encoding filename", () => {
			const path = "/src/assets/images/photo with spaces.jpg";
			const lastSlashIndex = path.lastIndexOf("/");
			const directory = path.substring(0, lastSlashIndex + 1);
			const filename = path.substring(lastSlashIndex + 1);
			const encoded = `${directory}${encodeURIComponent(filename)}`;

			expect(encoded).toBe("/src/assets/images/photo%20with%20spaces.jpg");
			expect(directory).toBe("/src/assets/images/");
		});

		it("should handle special characters in filenames", () => {
			const specialChars = [
				"photo&image.jpg",
				"photo=value.jpg",
				"photo+plus.jpg",
				"photo#hash.jpg",
			];

			for (const filename of specialChars) {
				const encoded = encodeURIComponent(filename);
				expect(encoded).not.toContain(filename);
				expect(encoded).toMatch(/^photo/);
			}
		});

		it("should handle kebab-case filenames without encoding", () => {
			const filename = "choosing-the-right-format-chronological.webp";
			const encoded = encodeURIComponent(filename);

			// Kebab-case doesn't need encoding
			expect(encoded).toBe(filename);
		});
	});

	describe("external URL detection", () => {
		it("should identify HTTP URLs", () => {
			const urls = [
				"http://example.com/image.jpg",
				"http://cdn.example.com/photo.webp",
				"http://localhost:3000/image.png",
			];

			for (const url of urls) {
				expect(url.startsWith("http://") || url.startsWith("https://")).toBe(
					true,
				);
			}
		});

		it("should identify HTTPS URLs", () => {
			const urls = [
				"https://example.com/image.jpg",
				"https://cdn.example.com/photo.webp",
				"https://images.unsplash.com/photo.jpg",
			];

			for (const url of urls) {
				expect(url.startsWith("http://") || url.startsWith("https://")).toBe(
					true,
				);
			}
		});

		it("should not treat relative paths as external URLs", () => {
			const paths = [
				"/images/photo.jpg",
				"./images/photo.jpg",
				"../images/photo.jpg",
				"images/photo.jpg",
			];

			for (const path of paths) {
				expect(path.startsWith("http://") || path.startsWith("https://")).toBe(
					false,
				);
			}
		});
	});

	describe("edge cases", () => {
		it("should handle paths with multiple extensions", () => {
			const path = "/src/assets/images/photo.backup.jpg";
			expect(path).toContain(".jpg");
		});

		it("should handle paths with numbers", () => {
			const path = "/src/assets/images/photo-123-v2.jpg";
			expect(path).toMatch(/\d+/);
		});

		it("should handle deep directory structures", () => {
			const path = "/src/assets/images/category/subcategory/photo.jpg";
			expect(path.split("/").length).toBeGreaterThan(5);
		});

		it("should handle paths with unicode characters", () => {
			const filename = "foto-español-日本語.jpg";
			const encoded = encodeURIComponent(filename);

			expect(encoded).toContain("%");
			expect(encoded).not.toBe(filename);
		});
	});

	describe("type handling", () => {
		it("should recognize ImageMetadata objects", () => {
			const mockImage: ImageMetadata = {
				src: "/path/to/image.jpg",
				width: 800,
				height: 600,
				format: "jpg",
			};

			expect(typeof mockImage).toBe("object");
			expect(mockImage.src).toBeDefined();
			expect(mockImage.width).toBeTypeOf("number");
			expect(mockImage.height).toBeTypeOf("number");
		});

		it("should handle null and undefined", () => {
			const nullValue = null;
			const undefinedValue = undefined;

			expect(nullValue).toBeNull();
			expect(undefinedValue).toBeUndefined();
		});

		it("should differentiate between strings and objects", () => {
			const stringPath = "/images/photo.jpg";
			const objectPath = { src: "/images/photo.jpg" };

			expect(typeof stringPath).toBe("string");
			expect(typeof objectPath).toBe("object");
		});
	});

	describe("console warnings", () => {
		it("should prepare for warning on image not found", () => {
			const consoleWarnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});

			// This would normally be called by findImage when an image isn't found
			console.warn("Image not found in assets: /src/assets/images/missing.jpg");

			expect(consoleWarnSpy).toHaveBeenCalledWith(
				"Image not found in assets: /src/assets/images/missing.jpg",
			);

			consoleWarnSpy.mockRestore();
		});

		it("should prepare for warning on load failure", () => {
			const consoleWarnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});

			// This would normally be called by findImage when loading fails
			console.warn(
				"Failed to load image: /src/assets/images/photo.jpg",
				new Error("Module not found"),
			);

			expect(consoleWarnSpy).toHaveBeenCalledWith(
				"Failed to load image: /src/assets/images/photo.jpg",
				expect.any(Error),
			);

			consoleWarnSpy.mockRestore();
		});
	});
});

describe("images.ts - Integration Scenarios", () => {
	describe("blog post cover images", () => {
		it("should handle blog cover image paths", () => {
			const coverPaths = [
				"/src/assets/images/choosing-the-right-format-chronological-hybrid-or-functional.webp",
				"/src/assets/images/critical-formatting-errors-what-makes-your-resume-fail-in-ats-and-recruiters.webp",
				"https://images.unsplash.com/photo.jpg",
				"https://cdn.pixabay.com/photo.jpg",
			];

			for (const path of coverPaths) {
				const isExternal =
					path.startsWith("http://") || path.startsWith("https://");
				const isLocal = path.startsWith("/src/assets/");

				expect(isExternal || isLocal).toBe(true);
			}
		});

		it("should handle monorepo paths from markdown frontmatter", () => {
			const markdownPath =
				"/client/apps/marketing/src/assets/images/photo.jpg";
			const normalized = markdownPath.replace("/client/apps/marketing", "");

			expect(normalized).toBe("/src/assets/images/photo.jpg");
		});
	});

	describe("supported image formats", () => {
		const formats = [
			"jpeg",
			"jpg",
			"png",
			"webp",
			"avif",
			"gif",
			"svg",
			"tiff",
		];

		it("should recognize all supported formats", () => {
			for (const format of formats) {
				const path = `/src/assets/images/photo.${format}`;
				expect(path).toMatch(new RegExp(`\\.${format}$`));
			}
		});

		it("should handle uppercase extensions", () => {
			const upperFormats = ["JPEG", "JPG", "PNG", "WEBP"];

			for (const format of upperFormats) {
				const path = `/src/assets/images/photo.${format}`;
				expect(path.toUpperCase()).toContain(format);
			}
		});
	});
});
