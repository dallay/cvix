import type { ImageMetadata } from "astro";
import { isExternalUrl, isImageMetadata } from "./image-utils.js";

export interface ImageGlobs {
	[key: string]: () => Promise<{ default: ImageMetadata }>;
}

export interface ImageResolverConfig {
	/**
	 * List of base paths to strip from the image path relative to the monorepo root
	 * Example: ['/client/apps/marketing', '/client/apps/blog']
	 */
	basePaths?: string[];
	/**
	 * Custom aliases to map to /src/
	 * Example: { "~/assets/": "/src/assets/" }
	 */
	aliases?: Record<string, string>;
}

export const createImageResolver = (
	imageGlobs: ImageGlobs,
	config: ImageResolverConfig = {},
) => {
	/**
	 * Normalizes a file path by encoding only the filename part.
	 * @param path - Path with directory and filename
	 * @returns Normalized path with URL-encoded filename
	 */
	const normalizeFilename = (path: string): string => {
		const lastSlashIndex = path.lastIndexOf("/");
		if (lastSlashIndex === -1) return path;

		const directory = path.substring(0, lastSlashIndex + 1);
		const filename = path.substring(lastSlashIndex + 1);

		// Only encode the filename part, not the directory path
		// This handles spaces and special characters in filenames
		const encodedFilename = encodeURIComponent(filename);

		return `${directory}${encodedFilename}`;
	};

	/**
	 * Normalized lookup map for fast case-insensitive image searches.
	 * Built once at module initialization to avoid expensive glob iterations.
	 * Maps: lowercased-path -> original-key-in-imageGlobs
	 */
	const normalizedLookupMap = new Map<string, string>();

	/**
	 * Build the normalized lookup map once at module initialization.
	 * This eliminates the need to iterate over all glob keys on every lookup.
	 */
	const buildNormalizedLookupMap = (): void => {
		const globKeys = Object.keys(imageGlobs);

		for (const key of globKeys) {
			// Store both the original lowercased key and the encoded version
			const lowercasedKey = key.toLowerCase();
			normalizedLookupMap.set(lowercasedKey, key);

			// Also store the encoded version for filenames with spaces
			// NOTE: Line 49 is uncovered by tests because it only executes for images
			// with spaces/special chars in filenames. Our project follows kebab-case naming.
			const encodedKey = normalizeFilename(key).toLowerCase();
			if (encodedKey !== lowercasedKey) {
				normalizedLookupMap.set(encodedKey, key);
			}
		}
	};

	// Initialize the lookup map once at module load
	buildNormalizedLookupMap();

	/**
	 * Rebuild the normalized lookup map.
	 * Useful if imageGlobs changes dynamically (rare in typical Astro builds).
	 * @returns Number of entries in the rebuilt map
	 */
	const rebuildImageLookupMap = (): number => {
		normalizedLookupMap.clear();
		buildNormalizedLookupMap();
		return normalizedLookupMap.size;
	};

	/**
	 * Get image lookup statistics for monitoring/debugging.
	 * @returns Metrics about the image lookup system
	 */
	interface ImageLookupStats {
		totalGlobKeys: number;
		normalizedMapSize: number;
		timestamp: string;
	}

	const getImageLookupStats = (): ImageLookupStats => {
		return {
			totalGlobKeys: Object.keys(imageGlobs).length,
			normalizedMapSize: normalizedLookupMap.size,
			timestamp: new Date().toISOString(),
		};
	};

	/**
	 * Find and resolve an image from the assets directory
	 * @param imagePath - Path to the image (supports ~/assets/images/*, @/assets/images/*, or src/assets/images/* format)
	 * @returns ImageMetadata or the original path
	 */
	const findImage = async (
		imagePath?: string | ImageMetadata | null,
	): Promise<string | ImageMetadata | undefined | null> => {
		// Not string - return as-is (already resolved or null)
		if (typeof imagePath !== "string") {
			return imagePath;
		}

		// Absolute URLs - return as-is
		if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
			return imagePath;
		}

		// Normalize path to /src/ format for glob lookup
		let normalizedPath = imagePath;

		// Handle configured base paths (e.g., /client/apps/marketing)
		if (config.basePaths) {
			for (const basePath of config.basePaths) {
				if (normalizedPath.startsWith(basePath)) {
					normalizedPath = normalizedPath.replace(basePath, "");
					break; // Stop after first match
				}
			}
		}

		// Handle aliases (default to ~ and @ if not provided, or merge - strict logic here assumes either/or or specific handling)
		const aliases = config.aliases || {
			"~/assets/": "/src/assets/",
			"@/assets/": "/src/assets/",
		};

		for (const [alias, replacement] of Object.entries(aliases)) {
			if (normalizedPath.startsWith(alias)) {
				normalizedPath = normalizedPath.replace(alias, replacement);
				break;
			}
		}

		// Explicit /src/ check from previous logic
		if (normalizedPath.startsWith("src/assets/")) {
			normalizedPath = `/${normalizedPath}`;
		}

		if (
			normalizedPath.startsWith("/") &&
			!normalizedPath.startsWith("/src/assets/")
		) {
			if (!normalizedPath.startsWith("/src/assets/")) {
				return imagePath; // Return ORIGINAL path if normalization didn't yield a src/assets path
			}
		} else if (!normalizedPath.startsWith("/src/assets/")) {
			// Path doesn't match expected patterns, return as-is
			return imagePath;
		}

		// Try to find the image in the glob results using a 3-tier lookup strategy:

		// 1. Fast path: exact match (O(1))
		let imageLoader = imageGlobs[normalizedPath];

		// 2. Fast path: encoded filename match (O(1))
		if (!imageLoader) {
			const encodedPath = normalizeFilename(normalizedPath);
			imageLoader = imageGlobs[encodedPath];
		}

		// 3. Optimized fallback: case-insensitive lookup using pre-built map (O(1))
		if (!imageLoader) {
			// Try direct lowercase match first
			const lowercasedPath = normalizedPath.toLowerCase();
			const matchingKey = normalizedLookupMap.get(lowercasedPath);

			if (matchingKey) {
				imageLoader = imageGlobs[matchingKey];
			} else {
				// Try encoded lowercase as final fallback
				const encodedLowercasedPath =
					normalizeFilename(normalizedPath).toLowerCase();
				const encodedMatchingKey = normalizedLookupMap.get(
					encodedLowercasedPath,
				);

				if (encodedMatchingKey) {
					imageLoader = imageGlobs[encodedMatchingKey];
				}
			}
		}

		if (typeof imageLoader === "function") {
			try {
				const module = await imageLoader();
				return module.default;
			} catch (err) {
				console.warn(`Failed to load image: ${normalizedPath}`, err);
				return null;
			}
		}

		// Image not found in glob results
		console.warn(`Image not found in assets: ${normalizedPath}`);
		return null;
	};

	/**
	 * Safely resolve ImageMetadata for a given path, returning null on failure.
	 */
	const safeFindImage = async (path: string): Promise<ImageMetadata | null> => {
		try {
			const meta = await findImage(path);
			return isImageMetadata(meta) ? meta : null;
		} catch (err) {
			// Non-fatal: callers can fall back to the original string
			console.warn(
				`prepareImageForOptimizedPicture: findImage failed for ${path}`,
				err,
			);
			return null;
		}
	};

	/**
	 * Prepare an image value for use with an optimized picture component.
	 *
	 * - If `imagePath` is already an object (e.g. ImageMetadata), it's returned.
	 * - If `imagePath` is an external URL (starts with 'http') it's returned as-is.
	 * - Otherwise, attempt to resolve via `findImage` and return ImageMetadata or
	 *   fallback to the original string.
	 */
	const prepareImageForOptimizedPicture = async (
		imagePath: unknown,
	): Promise<ImageMetadata | string | unknown> => {
		if (imagePath !== null && typeof imagePath === "object") return imagePath;
		if (typeof imagePath !== "string") return imagePath;
		if (isExternalUrl(imagePath)) return imagePath;

		const metadata = await safeFindImage(imagePath);
		if (metadata) return metadata;

		return imagePath;
	};

	return {
		findImage,
		rebuildImageLookupMap,
		getImageLookupStats,
		normalizeFilename,
		prepareImageForOptimizedPicture,
	};
};
