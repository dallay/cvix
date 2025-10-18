import { describe, expect, it } from "vitest";
import { randomElement } from "./random-element";

describe("randomElement", () => {
	it("should return an element from the array", () => {
		const array = [1, 2, 3, 4, 5];
		const element = randomElement(array);
		expect(array).toContain(element);
	});

	it("should throw an error for an empty array", () => {
		const array: number[] = [];
		expect(() => randomElement(array)).toThrow(
			"Cannot get random element from an empty array",
		);
	});

	it("should return the only element for a single-element array", () => {
		const array = [42];
		const element = randomElement(array);
		expect(element).toBe(42);
	});

	it("should handle arrays with different types", () => {
		const array = ["a", 1, true, null];
		const element = randomElement(array);
		expect(array).toContain(element);
	});

	it("should work with array of objects", () => {
		const array = [{ id: 1 }, { id: 2 }, { id: 3 }];
		const element = randomElement(array);
		expect(array).toContain(element);
	});

	it("should handle large arrays", () => {
		const array = Array.from({ length: 1000 }, (_, i) => i);
		const element = randomElement(array);
		expect(array).toContain(element);
	});

	it("should produce different results over multiple calls", () => {
		const array = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
		const results = new Set();

		// Run multiple times to ensure we get different values
		// Note: This test could theoretically fail by chance, but it's extremely unlikely
		for (let i = 0; i < 50; i++) {
			results.add(randomElement(array));
		}

		// We should have at least 2 different results after multiple calls
		expect(results.size).toBeGreaterThan(1);
	});
});
