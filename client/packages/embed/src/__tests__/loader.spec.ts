/** @vitest-environment jsdom */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { cvixLoader } from "../loader";

function clearDOM() {
	document.body.innerHTML = "";
	const scripts = Array.from(document.getElementsByTagName("script"));
	for (const s of scripts) s.remove();
}

function addContainer(id = "c1") {
	const el = document.createElement("div");
	el.setAttribute("data-cvix-form-id", id);
	document.body.appendChild(el);
	return el;
}

function addLoaderScript(src: string) {
	const s = document.createElement("script");
	s.src = src;
	document.head.appendChild(s);
	return s;
}

describe("cvixLoader", () => {
	beforeEach(() => {
		clearDOM();
	});

	afterEach(() => {
		vi.restoreAllMocks();
	});

	it("no containers - does nothing", async () => {
		const importer = vi.fn();
		await cvixLoader(
			document,
			importer as unknown as (p: string) => Promise<unknown>,
		);
		expect(importer).not.toHaveBeenCalled();
	});

	it("single container and loader script present - imports embed.js and calls init", async () => {
		addContainer();
		addLoaderScript("https://example.com/assets/loader.js");

		const init = vi.fn();
		const importer = vi.fn().mockResolvedValue({ init });

		await cvixLoader(
			document,
			importer as unknown as (p: string) => Promise<unknown>,
		);

		expect(importer).toHaveBeenCalledTimes(1);
		expect(importer).toHaveBeenCalledWith(
			"https://example.com/assets/embed.js",
		);
		expect(init).toHaveBeenCalled();
	});

	it("multiple containers - only imports once and calls init once", async () => {
		addContainer("a");
		addContainer("b");
		addLoaderScript("https://cdn.test/loader.js");

		const init = vi.fn();
		const importer = vi.fn().mockResolvedValue({ init });

		await cvixLoader(
			document,
			importer as unknown as (p: string) => Promise<unknown>,
		);

		expect(importer).toHaveBeenCalledTimes(1);
		expect(init).toHaveBeenCalledTimes(1);
	});

	it("no loader script - uses relative embed.js and calls init if present", async () => {
		addContainer();
		// no loader script added

		const init = vi.fn();
		const importer = vi.fn().mockResolvedValue({ init });

		await cvixLoader(
			document,
			importer as unknown as (p: string) => Promise<unknown>,
		);

		expect(importer).toHaveBeenCalledWith("embed.js");
		expect(init).toHaveBeenCalled();
	});

	it("importer throws - logs an error", async () => {
		addContainer();
		addLoaderScript("/loader.js");

		const importer = vi.fn().mockRejectedValue(new Error("fail"));
		const spy = vi.spyOn(console, "error").mockImplementation(() => {});

		await cvixLoader(
			document,
			importer as unknown as (p: string) => Promise<unknown>,
		);

		expect(spy).toHaveBeenCalled();
	});

	it("module has no init or init is not a function - no calls made", async () => {
		addContainer();
		addLoaderScript("/loader.js");

		const importer = vi.fn().mockResolvedValue({ init: "not-a-fn" });

		await cvixLoader(
			document,
			importer as unknown as (p: string) => Promise<unknown>,
		);

		// resolved but init not callable - nothing thrown
		expect(importer).toHaveBeenCalled();
	});
});
