import { createImageResolver } from "@cvix/astro-ui/image/images.ts";
import type { ImageMetadata } from "astro";

const images = import.meta.glob<{ default: ImageMetadata }>(
	"/src/assets/**/*.{jpeg,jpg,png,tiff,webp,gif,svg,avif}",
);

export const {
	findImage,
	getImageLookupStats,
	rebuildImageLookupMap,
	prepareImageForOptimizedPicture,
} = createImageResolver(images, {
	basePaths: ["/client/apps/blog"],
});
