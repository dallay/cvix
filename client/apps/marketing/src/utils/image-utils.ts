import type { ImageMetadata } from "astro";
import { findImage } from "@/utils/images";

/**
 * Type guard for Astro ImageMetadata objects.
 */
export function isImageMetadata(img: unknown): img is ImageMetadata {
	return !!img && typeof img === "object" && "src" in img && "width" in img && "height" in img;
}

/**
 * Type guard for external URLs (http/https).
 */
export function isExternalUrl(img: unknown): img is string {
	return typeof img === "string" && (img.startsWith("http://") || img.startsWith("https://"));
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
	return (
		typeof imagePath === "string" &&
		!imagePath.startsWith("http") &&
		!imagePath.startsWith("/")
	);
}

/**
 * Safely resolve ImageMetadata for a given path, returning null on failure.
 */
async function safeFindImage(path: string): Promise<ImageMetadata | null> {
	try {
		const meta = await findImage(path);
		return (meta as ImageMetadata) ?? null;
	} catch (err) {
		// Non-fatal: callers can fall back to the original string
		console.warn(
			`prepareImageForOptimizedPicture: findImage failed for ${path}`,
			err,
		);
		return null;
	}
}

/**
 * Prepare an image value for use with an optimized picture component.
 *
 * - If `imagePath` is already an object (e.g. ImageMetadata), it's returned.
 * - If `imagePath` is an external URL (starts with 'http') it's returned as-is.
 * - Otherwise, attempt to resolve via `findImage` and return ImageMetadata or
 *   fallback to the original string.
 */
export async function prepareImageForOptimizedPicture(
	imagePath: unknown,
): Promise<ImageMetadata | string | unknown> {
	if (imagePath !== null && typeof imagePath === "object") return imagePath;
	if (typeof imagePath !== "string") return imagePath;
	if (isExternalUrl(imagePath)) return imagePath;

	const metadata = await safeFindImage(imagePath);
	if (metadata) return metadata;

	return imagePath;
}

export default prepareImageForOptimizedPicture;
