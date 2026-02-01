// Loader for integrators - listens for postMessage from iframe and updates iframe height
(() => {
	const allowedOrigin = location.origin; // will be the embed domain

	window.addEventListener(
		"message",
		(e) => {
			// allow any origin for dev, but in prod check e.origin === allowedOrigin
			const d = e.data || {};
			const id = d.id;
			if (!id) return;
			const iframe =
				document.querySelector('iframe[src*="' + id + '"]') ||
				document.querySelector('iframe[data-form-id="' + id + '"]');
			if (!iframe) return;

			switch (d.type) {
				case "cvix:ready":
					iframe.dispatchEvent(new CustomEvent("cvix:ready", { detail: d }));
					break;
				case "cvix:height":
					iframe.style.height = (d.height || 100) + "px";
					break;
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
