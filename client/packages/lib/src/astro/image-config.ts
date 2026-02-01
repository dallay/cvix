/**
 * Default Sharp image service configuration for Astro.
 * Uses the Sharp image processing library with relaxed pixel limits.
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
