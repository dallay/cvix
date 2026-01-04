import path from "node:path";
import { test as base } from "@playwright/test";

/**
 * HAR Fixture for API mocking
 *
 * Usage:
 * 1. Record HAR: RECORD_HAR=true pnpm test:e2e (requires backend running)
 * 2. Replay HAR: USE_HAR=true pnpm test:e2e (default in CI, no backend needed)
 * 3. Normal mode: pnpm test:e2e (requires backend running)
 */

const useHarMocking =
	process.env.USE_HAR === "true" || process.env.CI === "true";
const recordHar = process.env.RECORD_HAR === "true";
const harPath = path.join(__dirname, "har", "api-responses.har");

export const test = base.extend({
	context: async ({ context }, use) => {
		if (recordHar) {
			// Recording mode: Capture all API calls to HAR file
			console.log("🔴 Recording HAR file to:", harPath);
			await context.routeFromHAR(harPath, {
				update: true, // Update HAR file with new requests
				updateContent: "embed", // Embed response bodies
				updateMode: "minimal", // Only update missing entries
				url: /\/api\//, // Only record API calls
			});
		} else if (useHarMocking) {
			// Replay mode: Mock API responses from HAR file
			console.log("▶️  Replaying HAR file from:", harPath);
			await context.routeFromHAR(harPath, {
				url: /\/api\//, // Only mock API calls
				notFound: "fallback", // Fallback to network if route not in HAR
			});
		}
		// Normal mode: No HAR, pass through to real backend

		await use(context);
	},
});

export { expect } from "@playwright/test";
