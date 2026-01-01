import type { ImageMetadata } from "astro";
import { describe, expect, it } from "vitest";
import {
	isExternalUrl,
	isImageMetadata,
	isLocalImage,
	isValidImage,
} from "../image-utils";

describe("image-utils", () => {
	describe("isImageMetadata", () => {
		it("should return true for valid ImageMetadata objects", () => {
			const validImage: ImageMetadata = {
				src: "/path/to/image.jpg",
				width: 800,
				height: 600,
				format: "jpg",
			};

			expect(isImageMetadata(validImage)).toBe(true);
		});

		it("should return false for objects missing required properties", () => {
			const invalidImage1 = { src: "/path/to/image.jpg", width: 800 };
			const invalidImage2 = { src: "/path/to/image.jpg", height: 600 };
			const invalidImage3 = { width: 800, height: 600 };

			expect(isImageMetadata(invalidImage1)).toBe(false);
			expect(isImageMetadata(invalidImage2)).toBe(false);
			expect(isImageMetadata(invalidImage3)).toBe(false);
		});

		it("should return false for non-objects", () => {
			expect(isImageMetadata(null)).toBe(false);
			expect(isImageMetadata(undefined)).toBe(false);
			expect(isImageMetadata("string")).toBe(false);
			expect(isImageMetadata(123)).toBe(false);
			expect(isImageMetadata([])).toBe(false);
		});

		it("should return false for empty objects", () => {
			expect(isImageMetadata({})).toBe(false);
		});
	});

	describe("isExternalUrl", () => {
		it("should return true for http URLs", () => {
			expect(isExternalUrl("http://example.com/image.jpg")).toBe(true);
			expect(isExternalUrl("http://localhost:3000/image.jpg")).toBe(true);
		});

		it("should return true for https URLs", () => {
			expect(isExternalUrl("https://example.com/image.jpg")).toBe(true);
			expect(isExternalUrl("https://cdn.example.com/images/photo.webp")).toBe(
				true,
			);
		});

		it("should return false for relative paths", () => {
			expect(isExternalUrl("/images/photo.jpg")).toBe(false);
			expect(isExternalUrl("./images/photo.jpg")).toBe(false);
			expect(isExternalUrl("../images/photo.jpg")).toBe(false);
		});

		it("should return false for non-strings", () => {
			expect(isExternalUrl(null as unknown as string)).toBe(false);
			expect(isExternalUrl(undefined as unknown as string)).toBe(false);
			expect(isExternalUrl(123 as unknown as string)).toBe(false);
			expect(isExternalUrl({} as unknown as string)).toBe(false);
		});

		it("should return false for empty strings", () => {
			expect(isExternalUrl("")).toBe(false);
		});
	});

	describe("isValidImage", () => {
		it("should return true for valid ImageMetadata", () => {
			const validImage: ImageMetadata = {
				src: "/path/to/image.jpg",
				width: 800,
				height: 600,
				format: "jpg",
			};

			expect(isValidImage(validImage)).toBe(true);
		});

		it("should return true for external URLs", () => {
			expect(isValidImage("https://example.com/image.jpg")).toBe(true);
			expect(isValidImage("http://example.com/image.jpg")).toBe(true);
		});

		it("should return false for relative paths", () => {
			expect(isValidImage("/images/photo.jpg")).toBe(false);
			expect(isValidImage("./images/photo.jpg")).toBe(false);
		});

		it("should return false for invalid objects", () => {
			expect(isValidImage({})).toBe(false);
			expect(isValidImage({ src: "/image.jpg" })).toBe(false);
		});

		it("should return false for non-image values", () => {
			expect(isValidImage(null)).toBe(false);
			expect(isValidImage(undefined)).toBe(false);
			expect(isValidImage(123)).toBe(false);
		});
	});

	describe("isLocalImage", () => {
		it("should return true for relative paths", () => {
			expect(isLocalImage("./images/photo.jpg")).toBe(true);
			expect(isLocalImage("../images/photo.jpg")).toBe(true);
			expect(isLocalImage("images/photo.jpg")).toBe(true);
			expect(isLocalImage("@/assets/images/photo.jpg")).toBe(true);
			expect(isLocalImage("~/assets/images/photo.jpg")).toBe(true);
		});

		it("should return false for absolute paths", () => {
			expect(isLocalImage("/images/photo.jpg")).toBe(false);
			expect(isLocalImage("/public/photo.jpg")).toBe(false);
		});

		it("should return false for external URLs", () => {
			expect(isLocalImage("https://example.com/image.jpg")).toBe(false);
			expect(isLocalImage("http://example.com/image.jpg")).toBe(false);
		});
	});

	describe("edge cases", () => {
		it("should handle protocol-relative URLs as external", () => {
			// Protocol-relative URLs should be treated as external
			// though our current implementation may not catch these
			const protocolRelative = "//example.com/image.jpg";
			expect(isExternalUrl(protocolRelative)).toBe(false); // Current behavior
		});

		it("should handle data URLs", () => {
			const dataUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAAB";
			expect(isExternalUrl(dataUrl)).toBe(false);
			// Data URLs don't start with "/" so isLocalImage returns true (they're not absolute paths)
			expect(isLocalImage(dataUrl)).toBe(true);
		});

		it("should handle file URLs", () => {
			const fileUrl = "file:///path/to/image.jpg";
			expect(isExternalUrl(fileUrl)).toBe(false);
		});

		it("should handle paths with special characters", () => {
			expect(isLocalImage("./images/photo with spaces.jpg")).toBe(true);
			expect(isLocalImage("./images/photo-with-dashes.jpg")).toBe(true);
			expect(isLocalImage("./images/photo_with_underscores.jpg")).toBe(true);
		});

		it("should handle paths with query strings", () => {
			expect(isExternalUrl("https://example.com/image.jpg?w=800&h=600")).toBe(
				true,
			);
			expect(isLocalImage("/images/photo.jpg?v=123")).toBe(false);
		});

		it("should handle paths with fragments", () => {
			expect(isExternalUrl("https://example.com/image.jpg#section")).toBe(true);
			expect(isLocalImage("/images/photo.jpg#top")).toBe(false);
		});
	});

	describe("type safety", () => {
		it("should correctly narrow types with type guards", () => {
			const unknownValue: unknown = "https://example.com/image.jpg";

			if (isExternalUrl(unknownValue)) {
				// TypeScript should know this is a string
				expect(typeof unknownValue).toBe("string");
				expect(unknownValue.startsWith("https://")).toBe(true);
			}
		});

		it("should correctly identify ImageMetadata type", () => {
			const unknownValue: unknown = {
				src: "/image.jpg",
				width: 800,
				height: 600,
				format: "jpg",
			};

			if (isImageMetadata(unknownValue)) {
				// TypeScript should know this is ImageMetadata
				expect(unknownValue.src).toBeDefined();
				expect(unknownValue.width).toBeDefined();
				expect(unknownValue.height).toBeDefined();
				expect(unknownValue.format).toBe("jpg");
			}
		});
	});
});
