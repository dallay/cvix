/**
 * Cvix Embed Renderer
 * Handles fetching configuration and rendering the form in Shadow DOM
 */

interface FormConfig {
	id: string;
	header: string;
	description: string;
	inputPlaceholder: string;
	buttonText: string;
	buttonColor: string;
	backgroundColor: string;
	textColor: string;
	buttonTextColor: string;
	status: string;
	confirmationRequired: boolean;
}

const API_VERSION_HEADER = "application/vnd.api.v1+json";

export function init() {
	const containers = document.querySelectorAll("[data-cvix-form-id]");
	for (const container of Array.from(containers)) {
		if (container.shadowRoot) continue;
		renderForm(container as HTMLElement);
	}
}

async function renderForm(container: HTMLElement) {
	const formId = container.getAttribute("data-cvix-form-id");
	const apiUrl =
		container.getAttribute("data-cvix-api-url") || "http://localhost:8080";

	if (!formId) return;

	try {
		const response = await fetch(
			`${apiUrl}/api/v1/subscription-forms/${formId}`,
			{
				headers: {
					Accept: API_VERSION_HEADER,
				},
			},
		);

		if (!response.ok) {
			console.error(`[Cvix] Failed to fetch form config: ${response.status}`);
			return;
		}

		const config: FormConfig = await response.json();

		if (config.status !== "PUBLISHED") {
			console.warn(
				`[Cvix] Form ${formId} is not published (status: ${config.status})`,
			);
			return;
		}

		setupShadowDOM(container, config, apiUrl);
	} catch (error) {
		console.error("[Cvix] Error initializing form:", error);
	}
}

function setupShadowDOM(
	container: HTMLElement,
	config: FormConfig,
	apiUrl: string,
) {
	const shadow = container.attachShadow({ mode: "open" });

	const style = document.createElement("style");
	style.textContent = `
        :host {
            display: block;
            font-family: system-ui, -apple-system, sans-serif;
            --cvix-bg: ${config.backgroundColor};
            --cvix-text: ${config.textColor};
            --cvix-btn-bg: ${config.buttonColor};
            --cvix-btn-text: ${config.buttonTextColor};
        }
        .wrapper {
            background: var(--cvix-bg);
            color: var(--cvix-text);
            padding: 1.5rem;
            border-radius: 0.5rem;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            max-width: 400px;
        }
        h3 { margin: 0 0 0.5rem 0; font-size: 1.25rem; }
        p { margin: 0 0 1rem 0; font-size: 0.875rem; opacity: 0.9; }
        form { display: flex; flex-direction: column; gap: 0.75rem; }
        input {
            padding: 0.625rem;
            border: 1px solid rgba(0,0,0,0.1);
            border-radius: 0.375rem;
            font-size: 0.875rem;
        }
        button {
            background: var(--cvix-btn-bg);
            color: var(--cvix-btn-text);
            border: none;
            padding: 0.625rem;
            border-radius: 0.375rem;
            font-weight: 600;
            cursor: pointer;
            transition: opacity 0.2s;
        }
        button:hover { opacity: 0.9; }
        button:disabled { opacity: 0.5; cursor: not-allowed; }
        .success { color: #10b981; font-weight: 500; display: none; }
        .error { color: #ef4444; font-size: 0.75rem; display: none; margin-top: 0.25rem; }
    `;
	shadow.appendChild(style);

	const wrapper = document.createElement("div");
	wrapper.className = "wrapper";

	const title = document.createElement("h3");
	title.textContent = config.header;
	wrapper.appendChild(title);

	if (config.description) {
		const desc = document.createElement("p");
		desc.textContent = config.description;
		wrapper.appendChild(desc);
	}

	const form = document.createElement("form");
	const input = document.createElement("input");
	input.type = "email";
	input.placeholder = config.inputPlaceholder;
	input.required = true;
	form.appendChild(input);

	const errorMsg = document.createElement("div");
	errorMsg.className = "error";
	form.appendChild(errorMsg);

	const button = document.createElement("button");
	button.type = "submit";
	button.textContent = config.buttonText;
	form.appendChild(button);

	const successMsg = document.createElement("div");
	successMsg.className = "success";
	successMsg.textContent = "¡Gracias por suscribirte!";
	wrapper.appendChild(successMsg);

	wrapper.appendChild(form);
	shadow.appendChild(wrapper);

	form.addEventListener("submit", async (e) => {
		e.preventDefault();
		const email = input.value;

		button.disabled = true;
		errorMsg.style.display = "none";

		try {
			const correlationId = crypto.randomUUID();
			const response = await fetch(`${apiUrl}/api/subscribers`, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
					Accept: API_VERSION_HEADER,
					"X-Correlation-ID": correlationId,
				},
				body: JSON.stringify({
					email,
					source: "embed-form",
					language: navigator.language.startsWith("es") ? "es" : "en",
					formId: config.id,
					metadata: {
						url: window.location.href,
						referrer: document.referrer,
					},
				}),
			});

			if (response.ok) {
				form.style.display = "none";
				successMsg.style.display = "block";
				container.dispatchEvent(
					new CustomEvent("cvix:submit:success", { detail: { email } }),
				);
			} else {
				const errorData = await response.json();
				throw new Error(errorData.detail || "Error al suscribirse");
			}
		} catch (error: any) {
			errorMsg.textContent = error.message;
			errorMsg.style.display = "block";
			button.disabled = false;
			container.dispatchEvent(
				new CustomEvent("cvix:submit:error", { detail: { error } }),
			);
		}
	});

	container.dispatchEvent(new CustomEvent("cvix:ready"));
}

// Auto-init if loaded directly
if (typeof window !== "undefined") {
	// @ts-expect-error
	window.Cvix = { init };
}
