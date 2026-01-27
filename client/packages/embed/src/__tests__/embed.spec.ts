/** @vitest-environment jsdom */
import { beforeEach, afterEach, describe, it, expect, vi } from "vitest";
import { init } from "../embed";

const SAMPLE_CONFIG = {
	id: "form-1",
	header: "Suscríbete",
	description: "Recibe novedades",
	inputPlaceholder: "tu@correo.com",
	buttonText: "Enviar",
	buttonColor: "#111111",
	backgroundColor: "#ffffff",
	textColor: "#000000",
	buttonTextColor: "#ffffff",
	status: "PUBLISHED",
	confirmationRequired: false,
};

function clearDOM() {
	document.body.innerHTML = "";
	const scripts = Array.from(document.getElementsByTagName("script"));
	for (const s of scripts) s.remove();
}

function addContainer(id = "form-1", apiUrl?: string) {
	const el = document.createElement("div");
	el.setAttribute("data-cvix-form-id", id);
	if (apiUrl) el.setAttribute("data-cvix-api-url", apiUrl);
	document.body.appendChild(el);
	return el;
}

function flush() {
	return new Promise((res) => setTimeout(res, 0));
}

describe("embed renderer (init + render + submit)", () => {
	let fetchSpy: ReturnType<typeof vi.spyOn>;
	let uuidSpy: ReturnType<typeof vi.spyOn> | null = null;

	beforeEach(() => {
		clearDOM();
		// default spy that will be reconfigured per test
		fetchSpy = vi.spyOn(globalThis, "fetch").mockImplementation(async () => {
			return {
				ok: false,
				status: 404,
				json: async () => ({}),
			} as unknown as Response;
		});
		// stable UUID
		// @ts-expect-error - crypto may be present in the env
		uuidSpy = vi.spyOn(globalThis.crypto, "randomUUID").mockReturnValue("uuid-1");
	});

	afterEach(() => {
		vi.restoreAllMocks();
		// restore navigator.language if needed
	});

	it("does nothing when no containers present", async () => {
		await init();
		expect(fetchSpy).not.toHaveBeenCalled();
	});

	it("renders shadow DOM when form config is published", async () => {
		const container = addContainer("form-1", "http://api.test");

		fetchSpy.mockImplementationOnce(async (input: RequestInfo) => {
			// config fetch
			return {
				ok: true,
				json: async () => SAMPLE_CONFIG,
			} as unknown as Response;
		});

		await init();
		await flush();

		expect(container.shadowRoot).toBeTruthy();
		const shadow = container.shadowRoot as ShadowRoot;
		expect(shadow.querySelector("h3")?.textContent).toBe(SAMPLE_CONFIG.header);
		expect(shadow.querySelector("form")).toBeTruthy();
	});

	it("form submit success hides form and emits cvix:submit:success", async () => {
		const container = addContainer("form-1", "http://api.test");

		// GET config
		fetchSpy.mockImplementationOnce(async (input: RequestInfo) => {
			return { ok: true, json: async () => SAMPLE_CONFIG } as unknown as Response;
		});

		// POST subscriber success
		fetchSpy.mockImplementationOnce(async (input: RequestInfo, init) => {
			// ensure method and body
			expect(typeof input === "string" ? input : input.url).toContain("/api/subscribers");
			return { ok: true, json: async () => ({}) } as unknown as Response;
		});

		await init();
		await flush();
		const shadow = container.shadowRoot as ShadowRoot;
		const form = shadow.querySelector("form") as HTMLFormElement;
		const input = form.querySelector("input") as HTMLInputElement;
		input.value = "test@example.com";

		const successPromise = new Promise<CustomEvent>((resolve) => {
			container.addEventListener("cvix:submit:success", (ev: Event) => resolve(ev as CustomEvent), { once: true });
		});

		form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
		await flush();

		const ev = await successPromise;
		expect(ev.detail).toEqual({ email: "test@example.com" });
		// form hidden and success visible
		expect((form as HTMLElement).style.display).toBe("none");
		const successMsg = shadow.querySelector(".success") as HTMLElement;
		expect(getComputedStyle(successMsg).display).not.toBe("none");
	});

	it("form submit failure shows error and emits cvix:submit:error", async () => {
		const container = addContainer("form-1", "http://api.test");

		// GET config
		fetchSpy.mockImplementationOnce(async () => {
			return { ok: true, json: async () => SAMPLE_CONFIG } as unknown as Response;
		});

		// POST subscriber failure
		fetchSpy.mockImplementationOnce(async () => {
			return {
				ok: false,
				json: async () => ({ detail: "Correo inválido" }),
			} as unknown as Response;
		});

		await init();
		await flush();
		const shadow = container.shadowRoot as ShadowRoot;
		const form = shadow.querySelector("form") as HTMLFormElement;
		const input = form.querySelector("input") as HTMLInputElement;
		input.value = "bad-email";

		const errorPromise = new Promise<CustomEvent>((resolve) => {
			container.addEventListener("cvix:submit:error", (ev: Event) => resolve(ev as CustomEvent), { once: true });
		});

		form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
		await flush();

		const ev = await errorPromise;
		expect(ev.detail).toHaveProperty("error");
		const errorDiv = shadow.querySelector(".error") as HTMLElement;
		expect(errorDiv.style.display).toBe("block");
	});

	it("logs error when config fetch returns non-ok", async () => {
		const container = addContainer("form-1", "http://api.test");
		const spy = vi.spyOn(console, "error").mockImplementation(() => {});

		fetchSpy.mockImplementationOnce(async () => ({ ok: false, status: 500, json: async () => ({}) }) as unknown as Response);

		await init();
		expect(spy).toHaveBeenCalled();
		expect(container.shadowRoot).toBeFalsy();
	});
});
