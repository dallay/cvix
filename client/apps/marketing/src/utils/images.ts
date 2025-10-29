import type { ImageMetadata } from "astro";

const load = async () => {
	let images: Record<string, () => Promise<unknown>> | undefined;
	try {
		images = import.meta.glob(
			"~/assets/images/**/*.{jpeg,jpg,png,tiff,webp,gif,svg,avif,JPEG,JPG,PNG,TIFF,WEBP,GIF,SVG,AVIF}",
		);
	} catch {
		// continue regardless of error
	}
	return images;
};

let _images: Record<string, () => Promise<unknown>> | undefined;

/** Fetch all local images from assets */
export const fetchLocalImages = async () => {
	_images = _images || (await load());
	return _images;
};

/**
 * Find and resolve an image from the assets directory
 * @param imagePath - Path to the image (supports ~/assets/images/* or @/assets/images/* format)
 * @returns ImageMetadata or the original path
 */
export const findImage = async (
	imagePath?: string | ImageMetadata | null,
): Promise<string | ImageMetadata | undefined | null> => {
	// Not string
	if (typeof imagePath !== "string") {
		return imagePath;
	}

	// Absolute paths
	if (
		imagePath.startsWith("http://") ||
		imagePath.startsWith("https://") ||
		imagePath.startsWith("/")
	) {
		return imagePath;
	}

	// Relative paths or not "~/assets/" or "@/assets/"
	if (
		!imagePath.startsWith("~/assets/images") &&
		!imagePath.startsWith("@/assets/images")
	) {
		return imagePath;
	}

	const images = await fetchLocalImages();
	// Convert both ~ and @ to /src/
	const key = imagePath.replace(/^[~@]\//, "/src/");

	return images && typeof images[key] === "function"
		? ((await images[key]()) as { default: ImageMetadata }).default
		: null;
};
