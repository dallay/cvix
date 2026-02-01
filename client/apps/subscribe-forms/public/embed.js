// Loader for integrators - listens for postMessage from iframe and updates iframe height
(() => {
	const allowedOrigin = location.origin; // will be the embed domain

	/**
	 * Safely locate iframe by form ID without selector injection
	 * Iterates through all iframes and matches by src attribute or data-form-id
	 * @param {string} id - The form ID to search for
	 * @returns {HTMLIFrameElement|null} The matching iframe or null
	 */
	function findIframeByFormId(id) {
		if (!id || typeof id !== "string") return null;

		const iframes = document.querySelectorAll("iframe");
		for (const iframe of iframes) {
			// Check data-form-id attribute (safer than selector interpolation)
			const dataFormId = iframe.getAttribute("data-form-id");
			if (dataFormId === id) return iframe;

			// Check src attribute for the form ID
			const src = iframe.getAttribute("src");
			if (src && src.includes(id)) return iframe;
		}
		return null;
	}

	/**
	 * Validate and sanitize height value from postMessage
	 * Ensures the height is a safe numeric value within acceptable bounds
	 * @param {*} height - Raw height value from message
	 * @returns {number} Sanitized height in pixels (100-2000 range)
	 */
	function sanitizeHeight(height) {
		// Default fallback
		const DEFAULT_HEIGHT = 100;
		const MAX_HEIGHT = 2000;
		const MIN_HEIGHT = 100;

		// Handle null/undefined
		if (height == null) return DEFAULT_HEIGHT;

		// Convert to number
		const numHeight = Number(height);

		// Validate it's a finite number
		if (!Number.isFinite(numHeight)) return DEFAULT_HEIGHT;

		// Validate it's positive
		if (numHeight <= 0) return DEFAULT_HEIGHT;

		// Clamp to safe range
		return Math.min(Math.max(numHeight, MIN_HEIGHT), MAX_HEIGHT);
	}

	window.addEventListener(
		"message",
		(e) => {
			// Security: Only accept messages from the allowed origin (embed domain)
			// This prevents XSS attacks from malicious sites sending postMessages
			if (e.origin !== allowedOrigin) {
				// Silently reject messages from unknown origins
				return;
			}

			const d = e.data || {};
			const id = d.id;
			if (!id) return;

			// Security: Use safe iframe lookup instead of selector interpolation
			// Prevents CSS selector injection attacks via malicious id values
			const iframe = findIframeByFormId(id);
			if (!iframe) return;

			switch (d.type) {
				case "cvix:ready":
					iframe.dispatchEvent(new CustomEvent("cvix:ready", { detail: d }));
					break;
				case "cvix:height": {
					// Security: Validate and sanitize height before applying
					// Prevents CSS injection via malicious height values like "100px; malicious-code"
					const safeHeight = sanitizeHeight(d.height);
					iframe.style.height = safeHeight + "px";
					break;
				}
				case "cvix:submit:success":
					iframe.dispatchEvent(
						new CustomEvent("cvix:submit:success", { detail: d }),
					);
					break;
				case "cvix:submit:error":
					iframe.dispatchEvent(
						new CustomEvent("cvix:submit:error", { detail: d }),
					);
					break;
			}
		},
		false,
	);
})();
