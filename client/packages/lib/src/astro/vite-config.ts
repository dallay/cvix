/**
 * List of @cvix packages that should not be externalized during SSR.
 * These packages need to be bundled to work correctly in the Astro SSR environment.
 */
export const CVIX_SSR_NO_EXTERNAL = [
	"@cvix/assets",
	"@cvix/astro-ui",
	"@cvix/i18n",
	"@cvix/lib",
	"@cvix/tsconfig",
	"@cvix/ui",
	"@cvix/utilities",
] as const;

/**
 * Type for the SSR no external list
 */
export type CvixSsrNoExternal = (typeof CVIX_SSR_NO_EXTERNAL)[number];
