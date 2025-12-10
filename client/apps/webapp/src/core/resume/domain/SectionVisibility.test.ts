import { describe, expect, it } from "vitest";
import {
	type ArraySectionVisibility,
	countVisibleItems,
	hasVisibleItems,
} from "./SectionVisibility";

describe("countVisibleItems", () => {
	it("returns 0 if section is disabled", () => {
		const vis: ArraySectionVisibility = {
			enabled: false,
			expanded: false,
			items: [true, true, false],
		};
		expect(countVisibleItems(vis)).toBe(0);
	});
	it("counts only true values", () => {
		const vis: ArraySectionVisibility = {
			enabled: true,
			expanded: false,
			items: [true, false, true, false],
		};
		expect(countVisibleItems(vis)).toBe(2);
	});
	it("returns 0 for empty items array", () => {
		const vis: ArraySectionVisibility = {
			enabled: true,
			expanded: false,
			items: [],
		};
		expect(countVisibleItems(vis)).toBe(0);
	});
});

describe("hasVisibleItems", () => {
	it("returns false if section is disabled", () => {
		const vis: ArraySectionVisibility = {
			enabled: false,
			expanded: false,
			items: [true, true],
		};
		expect(hasVisibleItems(vis)).toBe(false);
	});
	it("returns true if at least one item is visible", () => {
		const vis: ArraySectionVisibility = {
			enabled: true,
			expanded: false,
			items: [false, true, false],
		};
		expect(hasVisibleItems(vis)).toBe(true);
	});
	it("returns false if no items are visible", () => {
		const vis: ArraySectionVisibility = {
			enabled: true,
			expanded: false,
			items: [false, false],
		};
		expect(hasVisibleItems(vis)).toBe(false);
	});
	it("returns false for empty items array", () => {
		const vis: ArraySectionVisibility = {
			enabled: true,
			expanded: false,
			items: [],
		};
		expect(hasVisibleItems(vis)).toBe(false);
	});
});
