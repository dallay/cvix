import type { ImageMetadata } from "astro";

/**
 * Type guard for Astro ImageMetadata objects.
 */
export function isImageMetadata(img: unknown): img is ImageMetadata {
	return (
		!!img &&
		typeof img === "object" &&
		"src" in img &&
		"width" in img &&
		"height" in img
	);
}

/**
 * Type guard for external URLs (http/https).
 */
export function isExternalUrl(img: unknown): img is string {
	return (
		typeof img === "string" &&
		(img.startsWith("http://") || img.startsWith("https://"))
	);
}

/**
 * Check if the image is valid for rendering (either ImageMetadata or external URL).
 */
export function isValidImage(img: unknown): img is ImageMetadata | string {
	return isImageMetadata(img) || isExternalUrl(img);
}

/**
 * Returns true when the provided path refers to a local asset file
 * (relative path inside the source tree) and not an absolute/public or remote URL.
 */
export function isLocalImage(imagePath: string): boolean {
	if (isExternalUrl(imagePath)) return false;
	return !(imagePath as string).startsWith("/");
}
