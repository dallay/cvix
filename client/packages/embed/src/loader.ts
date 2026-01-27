/**
 * Cvix Loader
 * Tiny script to find form containers and load the renderer
 */
(async function cvixLoader() {
	const containers = document.querySelectorAll("[data-cvix-form-id]");
	if (containers.length === 0) return;

	// Determine the base URL for loading embed.js
	// We look for the loader script to know where we are hosted
	const scripts = document.getElementsByTagName("script");
	let baseUrl = "";

	for (let i = 0; i < scripts.length; i++) {
		const src = scripts[i].src;
		if (src && src.includes("loader.js")) {
			baseUrl = src.substring(0, src.lastIndexOf("/") + 1);
			break;
		}
	}

	try {
		// Dynamically import the renderer
		const { init } = await import(/* @vite-ignore */ `${baseUrl}embed.js`);
		if (typeof init === "function") {
			init();
		}
	} catch (error) {
		console.error("[Cvix] Failed to load embed renderer:", error);
	}
})();
