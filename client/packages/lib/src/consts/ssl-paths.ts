import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

/**
 * Shared SSL certificate paths for development HTTPS configuration.
 * Used by both Playwright and Astro configurations.
 *
 * These paths are resolved relative to this package file and exported as
 * absolute filesystem paths, ensuring they work correctly regardless of
 * import depth or consumer location within the monorepo.
 */

// Resolve the directory containing this module
const moduleDir = dirname(fileURLToPath(import.meta.url));

export const SSL_CERT_PATH = resolve(
	moduleDir,
	"../../../../../infra/ssl/localhost.pem",
);
export const SSL_KEY_PATH = resolve(
	moduleDir,
	"../../../../../infra/ssl/localhost-key.pem",
);
