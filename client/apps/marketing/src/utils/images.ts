import type { ImageMetadata } from "astro";

// Glob patterns for image assets - using relative path from src
const imageGlobs = import.meta.glob<{ default: ImageMetadata }>(
	"/src/assets/**/*.{jpeg,jpg,png,tiff,webp,gif,svg,avif,JPEG,JPG,PNG,TIFF,WEBP,GIF,SVG,AVIF}",
);

/**
 * Find and resolve an image from the assets directory
 * @param imagePath - Path to the image (supports ~/assets/images/*, @/assets/images/*, or src/assets/images/* format)
 * @returns ImageMetadata or the original path
 */
export const findImage = async (
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

	if (imagePath.startsWith("/client/apps/marketing/src/assets/")) {
		// Support full monorepo path format
		normalizedPath = imagePath.replace("/client/apps/marketing", "");
	} else if (
		imagePath.startsWith("~/assets/") ||
		imagePath.startsWith("@/assets/")
	) {
		normalizedPath = imagePath.replace(/^[~@]\//, "/src/");
	} else if (imagePath.startsWith("src/assets/")) {
		normalizedPath = `/${imagePath}`;
	} else if (imagePath.startsWith("/") && !imagePath.startsWith("/src/assets/")) {
		// Other absolute paths (like /public/) - return as-is
		return imagePath;
	} else if (!imagePath.startsWith("/src/assets/")) {
		// Path doesn't match expected patterns, return as-is
		return imagePath;
	}

	// Try to find the image in the glob results
	const imageLoader = imageGlobs[normalizedPath];

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
