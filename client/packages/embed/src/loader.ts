/**
 * Cvix Loader
 * Tiny script to find form containers and load the renderer
 */
export async function cvixLoader(
	documentParam: Document = document,
	importer: (path: string) => Promise<unknown> = (p: string) => import(/* @vite-ignore */ p),
) {
	const containers = documentParam.querySelectorAll("[data-cvix-form-id]");
	if (containers.length === 0) return;

	// Determine the base URL for loading embed.js
	// We look for the loader script to know where we are hosted
	const scripts = documentParam.getElementsByTagName("script");
	let baseUrl = "";

	for (let i = 0; i < scripts.length; i++) {
		const src = scripts[i].src;
		if (src?.includes("loader.js")) {
			baseUrl = src.substring(0, src.lastIndexOf("/") + 1);
			break;
		}
	}

	try {
		// Dynamically import the renderer using the injected importer
		const module = await importer(`${baseUrl}embed.js`);
		const init = (module as { init?: unknown })?.init;
		if (typeof init === "function") {
			(init as Function)();
		}
	} catch (error) {
		console.error("[Cvix] Failed to load embed renderer:", error);
	}
}

// Preserve existing behavior: immediately run the loader in the browser environment
(async function runDefaultLoader() {
	try {
		// Only call when running in a browser-like environment where `document` exists
		// eslint-disable-next-line @typescript-eslint/strict-boolean-expressions
		if (typeof document !== "undefined") {
			await cvixLoader();
		}
	} catch {
		// swallow any error from the default bootstrap to avoid breaking host pages
		// Errors in the import are already logged by cvixLoader
	}
})();
