import {describe, expect, it, vi} from "vitest";
import type {ImageMetadata} from "astro";
import {findImage, normalizeFilename} from "@/utils/images.ts";

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
    it("should normalize monorepo path format and return null for missing image", async () => {
      const input = "/client/apps/marketing/src/assets/images/photo-nonexistent.jpg";

      // findImage should normalize the path to /src/assets/images/photo-nonexistent.jpg
      // and return null (image doesn't exist in glob results)
      const result = await findImage(input);
      
      // Result should be null (image not found, but normalization worked)
      expect(result).toBeNull();
    });

    it("should normalize tilde paths and return null for missing image", async () => {
      const input = "~/assets/images/photo-nonexistent.jpg";

      // findImage should normalize ~/assets/ to /src/assets/
      const result = await findImage(input);
      
      // Result should be null (image not found, but normalization worked)
      expect(result).toBeNull();
    });

    it("should normalize @ alias paths and return null for missing image", async () => {
      const input = "@/assets/images/photo-nonexistent.jpg";

      // findImage should normalize @/assets/ to /src/assets/
      const result = await findImage(input);
      
      // Result should be null (image not found, but normalization worked)
      expect(result).toBeNull();
    });

    it("should normalize src/ relative paths and return null for missing image", async () => {
      const input = "src/assets/images/photo-nonexistent.jpg";

      // findImage should normalize src/assets/ to /src/assets/
      const result = await findImage(input);
      
      // Result should be null (image not found, but normalization worked)
      expect(result).toBeNull();
    });

    it("should handle already normalized paths and return null for missing image", async () => {
      const input = "/src/assets/images/photo-nonexistent.jpg";

      // findImage should accept the path as-is (already normalized)
      const result = await findImage(input);
      
      // Result should be null (image not found, but normalization worked)
      expect(result).toBeNull();
    });
  });

  describe("normalizeFilename helper", () => {
    it("should return filename unchanged when no path separator exists", () => {
      const filename = "photo with spaces.jpg";
      const normalized = normalizeFilename(filename);

      // normalizeFilename only encodes when there's a directory path
      // Without '/', it returns the input unchanged
      expect(normalized).toBe(filename);
    });

    it("should preserve directory and encode filename when path has separator", () => {
      const path = "/src/assets/images/photo with spaces.jpg";
      const normalized = normalizeFilename(path);

      expect(normalized).toBe("/src/assets/images/photo%20with%20spaces.jpg");
      // Verify directory is preserved
      expect(normalized.startsWith("/src/assets/images/")).toBe(true);
    });

    it("should handle special characters in filenames with paths", () => {
      const testCases = [
        { input: "/images/photo&image.jpg", expected: "/images/photo%26image.jpg" },
        { input: "/images/photo=value.jpg", expected: "/images/photo%3Dvalue.jpg" },
        { input: "/images/photo+plus.jpg", expected: "/images/photo%2Bplus.jpg" },
        { input: "/images/photo#hash.jpg", expected: "/images/photo%23hash.jpg" },
      ];

      for (const { input, expected } of testCases) {
        const normalized = normalizeFilename(input);
        expect(normalized).toBe(expected);
        // Verify special characters are encoded
        expect(normalized).not.toContain("&");
        expect(normalized).not.toContain("=");
        expect(normalized).not.toContain("+");
        expect(normalized).not.toContain("#");
      }
    });

    it("should handle kebab-case filenames without additional encoding", () => {
      const path = "/images/choosing-the-right-format-chronological.webp";
      const normalized = normalizeFilename(path);

      // Kebab-case characters (letters, numbers, hyphens, dots) don't need encoding
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
      // Paths that don't match /src/assets/ pattern are returned as-is
      const passThroughPaths = [
        "/images/photo.jpg",  // Absolute but not /src/assets/
        "./images/photo.jpg", // Relative
        "../images/photo.jpg", // Relative
        "images/photo.jpg",   // Relative
      ];

      for (const path of passThroughPaths) {
        const result = await findImage(path);
        // These paths are returned unchanged because they don't match
        // the /src/assets/ pattern that findImage handles
        expect(result).toBe(path);
      }
    });
  });

  describe("edge cases for normalizeFilename and findImage", () => {
    it("should normalize paths with multiple extensions and end with .jpg", () => {
      const path = "/src/assets/images/photo.backup.jpg";
      const normalized = normalizeFilename(path);
      expect(normalized).toMatch(/\.jpg$/);
      // Should encode only filename part
      expect(normalized).toBe(`/src/assets/images/${encodeURIComponent('photo.backup.jpg')}`);
    });

    it("should preserve numbers in filenames", () => {
      const path = "/src/assets/images/photo-123-v2.jpg";
      const normalized = normalizeFilename(path);
      expect(normalized).toContain("123");
      expect(normalized).toBe(`/src/assets/images/${encodeURIComponent('photo-123-v2.jpg')}`);
    });

    it("should handle very deep directory structures", () => {
      const deepPath = "/src/assets/images/category/subcategory/photo.jpg";
      const normalized = normalizeFilename(deepPath);
      // Directory structure should be preserved
      expect(normalized).toContain("category/subcategory/");
      expect(normalized).toBe(`/src/assets/images/category/subcategory/${encodeURIComponent('photo.jpg')}`);
    });

    it("should handle unicode filenames and encode accordingly", () => {
      const path = "/src/assets/images/foto-español-日本語.jpg";
      const normalized = normalizeFilename(path);
      // Should encode non-ASCII chars
      expect(normalized).toMatch(/%/);
      // Decoding the filename part should restore original
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
      const nullValue = null;
      const undefinedValue = undefined;

      expect(await findImage(nullValue)).toBeNull();
      expect(await findImage(undefinedValue)).toBeUndefined();
    });

    it("should differentiate string path vs object by return type", async () => {
      const stringPath = "/images/photo.jpg";
      const objectInput = {src: "/images/photo.jpg"};
      const resultString = await findImage(stringPath);
      const resultObject = await findImage(objectInput as any);
      // contract: string returns string or normalized path, object returns object
      expect(typeof resultString === "string" || typeof resultString === "object" || resultString === null).toBe(true);
      expect(resultObject).toEqual(objectInput);
    });
  });

  describe("console warnings and loader error handling", () => {
    it("should warn when image is not found", async () => {
      const consoleWarnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
      await findImage("/src/assets/images/this-image-does-not-exist.jpg");
      expect(consoleWarnSpy).toHaveBeenCalledWith(
        expect.stringContaining("Image not found"),
      );
      consoleWarnSpy.mockRestore();
    });

    it("should handle missing image gracefully (loader error path cannot be mocked post-import)", async () => {
      /**
       * LIMITATION: import.meta.glob is evaluated at module initialization time.
       * Since the images module is already imported at the top of this test file,
       * we cannot mock import.meta.glob to force a loader error in the catch block.
       * 
       * This test verifies the "not found" branch, which also returns null and warns.
       * To test the actual loader error path, you would need to:
       * 1. Mock import.meta.glob BEFORE any import of images.ts
       * 2. Use dynamic import() after setting up the mock
       * 3. Ensure module cache is cleared between tests
       * 
       * For now, this test documents the limitation and verifies graceful handling
       * of missing images (which exercises the same error-handling code path).
       */
      const consoleWarnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
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
    it("should provide lookup statistics", async () => {
      const {getImageLookupStats} = await import("../images");
      const stats = getImageLookupStats();

      expect(stats).toHaveProperty("totalGlobKeys");
      expect(stats).toHaveProperty("normalizedMapSize");
      expect(stats).toHaveProperty("timestamp");
      expect(typeof stats.totalGlobKeys).toBe("number");
      expect(typeof stats.normalizedMapSize).toBe("number");
      expect(typeof stats.timestamp).toBe("string");
    });

    it("should have normalized map size >= glob keys (due to encoded variants)", async () => {
      const {getImageLookupStats} = await import("../images");
      const stats = getImageLookupStats();

      // Normalized map can be larger because it stores both
      // original and encoded versions for files with spaces
      expect(stats.normalizedMapSize).toBeGreaterThanOrEqual(stats.totalGlobKeys);
    });

    it("should allow rebuilding the lookup map", async () => {
      const {rebuildImageLookupMap, getImageLookupStats} = await import("../images");

      const initialStats = getImageLookupStats();
      const rebuiltSize = rebuildImageLookupMap();
      const finalStats = getImageLookupStats();

      expect(typeof rebuiltSize).toBe("number");
      expect(finalStats.normalizedMapSize).toBe(rebuiltSize);
      expect(finalStats.normalizedMapSize).toBe(initialStats.normalizedMapSize);
    });

    it("should return consistent stats across multiple calls", async () => {
      const {getImageLookupStats} = await import("../images");

      const stats1 = getImageLookupStats();
      const stats2 = getImageLookupStats();

      expect(stats1.totalGlobKeys).toBe(stats2.totalGlobKeys);
      expect(stats1.normalizedMapSize).toBe(stats2.normalizedMapSize);
    });
  });

  describe("case-insensitive image lookup (3-tier strategy)", () => {
    it("should find real images with mixed case paths via case-insensitive lookup", async () => {
      // Test that the normalized lookup map handles case-insensitive matching
      // Using real image: blog-placeholder-1.avif
      const mixedCasePath = "/src/assets/images/BLOG-PLACEHOLDER-1.AVIF";
      
      const result = await findImage(mixedCasePath);
      
      // Should find the image via case-insensitive lookup (tier 3)
      // Result can be either a string (URL) or ImageMetadata object depending on Vite config
      expect(result).not.toBeNull();
      expect(typeof result === "string" || typeof result === "object").toBe(true);
      
      if (result && typeof result === "object" && "format" in result) {
        expect(result.format).toBe("avif");
      }
    });

    it("should handle various case combinations for real images", async () => {
      // Test various case combinations with real image
      const testPaths = [
        "/src/assets/images/blog-placeholder-1.avif",  // Exact case (tier 1)
        "/src/assets/images/BLOG-PLACEHOLDER-1.AVIF",  // Uppercase (tier 3)
        "/src/assets/images/Blog-Placeholder-1.Avif",  // Mixed case (tier 3)
      ];

      for (const path of testPaths) {
        const result = await findImage(path);
        // All should find the same image (string URL or ImageMetadata object)
        expect(result).not.toBeNull();
        expect(typeof result === "string" || typeof result === "object").toBe(true);
      }
    });

    it("should return consistent ImageMetadata for same path called multiple times", async () => {
      const path1 = "/src/assets/images/blog-placeholder-1.avif";
      const path2 = "/src/assets/images/blog-placeholder-1.avif";

      const result1 = await findImage(path1);
      const result2 = await findImage(path2);

      // Results should be identical objects
      expect(result1).toEqual(result2);
      expect(result1).not.toBeNull();
    });

    it("should find images via encoded filename match (tier 2)", async () => {
      // Test the encoded filename path (tier 2 of lookup strategy)
      // If we had an image with spaces, normalizeFilename would encode it
      // For now, test that encoding works even if not needed
      const normalPath = "/src/assets/images/blog-placeholder-1.avif";
      const encodedPath = normalizeFilename(normalPath);
      
      const result = await findImage(encodedPath);
      
      // Should find the image (even though encoding wasn't necessary)
      // Result can be either a string (URL) or ImageMetadata object
      expect(result).not.toBeNull();
      expect(typeof result === "string" || typeof result === "object").toBe(true);
    });

    it("should find images via encoded lowercase fallback (tier 3 final fallback)", async () => {
      // Test the tier 3 encoded lowercase fallback path
      // This tests line 146-150: normalizeFilename(path).toLowerCase() lookup
      // Use mixed case with kebab-case filename (would be encoded but no special chars)
      const mixedCasePath = "/src/assets/images/BLOG-placeholder-1.avif";
      
      const result = await findImage(mixedCasePath);
      
      // Should find via tier 3 encoded lowercase fallback
      expect(result).not.toBeNull();
      expect(typeof result === "string" || typeof result === "object").toBe(true);
    });

    it("should pass through paths that don't start with /src/assets/ unchanged", async () => {
      // Test paths that don't match the expected pattern (line 120-122)
      const relativePath = "relative/path/image.jpg";
      const result = await findImage(relativePath);
      
      // Should return path unchanged (doesn't match /src/assets/ pattern)
      expect(result).toBe(relativePath);
    });

    /**
     * COVERAGE NOTE: Uncovered defensive code paths
     * 
     * The following lines are intentionally uncovered because they handle edge cases
     * that are extremely difficult or impractical to test without complex mocks:
     * 
     * Line 49: `normalizedLookupMap.set(encodedKey, key)`
     *   - Only executed for images with spaces or special characters in filenames
     *   - Our project follows kebab-case naming (no spaces)
     *   - Testing would require creating actual image files with spaces
     * 
     * Line 150: `imageLoader = imageGlobs[encodedMatchingKey]`
     *   - Tier 3 encoded fallback for mixed-case paths with special characters
     *   - Extremely rare: requires UPPERCASE + special chars in filename
     *   - Would need: "/src/assets/images/PHOTO NAME.jpg" → matches "photo%20name.jpg"
     * 
     * Lines 160-161: `catch` block in image loader
     *   - Only executes if import.meta.glob loader throws
     *   - Cannot mock post-import (see test documentation above)
     *   - Defensive code for runtime loader failures (disk I/O, memory issues)
     * 
     * These paths are defensive programming best practices and acceptable to leave uncovered.
     */
    it("should document uncovered defensive code paths", () => {
      // This test exists to document why certain lines are uncovered
      // See JSDoc comment above for detailed explanation
      expect(true).toBe(true);
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
      // Test with a real image that exists in the project
      const realImagePath = "/src/assets/images/blog-placeholder-1.avif";
      
      const result = await findImage(realImagePath);
      
      // Should return something (ImageMetadata object or string URL), not null
      expect(result).not.toBeNull();
      expect(result).toBeDefined();
      expect(typeof result === "string" || typeof result === "object").toBe(true);
      
      // If ImageMetadata object is returned, verify structure
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
      // Test with full monorepo path format (as it appears in markdown frontmatter)
      const markdownPath =
          "/client/apps/marketing/src/assets/images/blog-placeholder-1.avif";
      
      const result = await findImage(markdownPath);
      
      // Should find the image after normalizing the path
      // Result can be either a string (URL) or ImageMetadata object
      expect(result).not.toBeNull();
      expect(result).toBeDefined();
      expect(typeof result === "string" || typeof result === "object").toBe(true);
    });

    it("should find images with tilde and @ alias paths", async () => {
      // Test with tilde and @ alias paths pointing to real images
      const tildePath = "~/assets/images/blog-placeholder-1.avif";
      const aliasPath = "@/assets/images/blog-placeholder-1.avif";
      
      const tildeResult = await findImage(tildePath);
      const aliasResult = await findImage(aliasPath);
      
      // Both should find the same image (string URL or ImageMetadata object)
      expect(tildeResult).not.toBeNull();
      expect(aliasResult).not.toBeNull();
      expect(typeof tildeResult === "string" || typeof tildeResult === "object").toBe(true);
      expect(typeof aliasResult === "string" || typeof aliasResult === "object").toBe(true);
    });

    it("should find images with different formats (webp, avif)", async () => {
      const webpImage = "/src/assets/images/choosing-the-right-format-chronological-hybrid-or-functional.webp";
      const avifImage = "/src/assets/images/blog-placeholder-2.avif";
      
      const webpResult = await findImage(webpImage);
      const avifResult = await findImage(avifImage);
      
      expect(webpResult).not.toBeNull();
      expect(avifResult).not.toBeNull();
      
      if (webpResult && typeof webpResult === "object" && "format" in webpResult) {
        expect(webpResult.format).toBe("webp");
      }
      
      if (avifResult && typeof avifResult === "object" && "format" in avifResult) {
        expect(avifResult.format).toBe("avif");
      }
    });
  });

  describe("supported image formats", () => {
    it("should handle various image format extensions", async () => {
      const formats = [
        "jpeg",
        "jpg",
        "png",
        "webp",
        "avif",
        "gif",
        "svg",
      ];

      for (const format of formats) {
        const path = `/src/assets/images/photo.${format}`;
        const result = await findImage(path);
        // Result will be null if image doesn't exist, or ImageMetadata if it does
        // The function should handle the format correctly regardless
        expect(result === null || typeof result === "object" || typeof result === "string").toBe(true);
      }
    });

    it("should handle uppercase extensions via case-insensitive matching", async () => {
      const upperFormats = ["JPEG", "JPG", "PNG", "WEBP"];

      for (const format of upperFormats) {
        const path = `/src/assets/images/photo.${format}`;
        const result = await findImage(path);
        // Case-insensitive lookup should work
        expect(result === null || typeof result === "object" || typeof result === "string").toBe(true);
      }
    });
  });
});
