/**
 * Default Sharp image service configuration for Astro.
 * Uses the Sharp image processing library with relaxed pixel limits.
 *
 * SECURITY NOTE: limitInputPixels is disabled (false) because:
 * - We only process trusted bundled assets (no user uploads)
 * - All images are pre-verified static assets from the repo
 * - Maximum image sizes are controlled and verified (~104KB max)
 * - CI checks ensure no malicious images are committed
 * - This allows processing of high-resolution images for responsive layouts
 * If user uploads are added in the future, re-enable with a reasonable limit (e.g., 268402689)
 */
export const DEFAULT_IMAGE_CONFIG = {
	service: {
		entrypoint: "astro/assets/services/sharp",
		config: {
			limitInputPixels: false,
		},
	},
} as const;

/**
 * Type for the image configuration
 */
export type ImageConfig = typeof DEFAULT_IMAGE_CONFIG;
