/**
 * @cvix/assets - Centralized assets for all CVIX frontend applications
 *
 * This package provides a single source of truth for all shared assets
 * including logos, images, icons, and public static files.
 *
 * Usage:
 * - Logos: import lightLogo from '@cvix/assets/logos/light-isotype.svg'
 * - Images: import placeholder from '@cvix/assets/images/blog/blog-placeholder-1.avif'
 * - Public files: Copy from node_modules/@cvix/assets/public/ to your app's public folder
 */

// Re-export asset paths for programmatic use
export const ASSETS_VERSION = "0.0.1";

// Brand paths (relative to package root)
export const LOGOS = {
	light: "logos/light-isotype.svg",
	dark: "logos/dark-isotype.svg",
} as const;

// Blog placeholder images
export const BLOG_PLACEHOLDERS = [
	"images/blog/blog-placeholder-1.avif",
	"images/blog/blog-placeholder-2.avif",
	"images/blog/blog-placeholder-3.avif",
	"images/blog/blog-placeholder-4.avif",
] as const;

// General images
export const IMAGES = {
	ctaDashboardMockup: "images/cta-dashboard-mockup.svg",
	ctaDashboardMockupDark: "images/cta-dashboard-mockup-dark.svg",
	videoPlaceholder: "images/video-placeholder.png",
	landingPage: "images/cvix-system.png",
	pet: "images/pet.svg",
	petPng: "images/pet.png",
} as const;
