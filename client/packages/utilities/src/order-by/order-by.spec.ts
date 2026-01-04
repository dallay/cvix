import { describe, expect, it } from "vitest";
import { orderBy } from "./order-by.js";

describe("orderBy", () => {
	it("sorts items by multiple keys and orders", () => {
		const result = orderBy(
			[
				{ name: "banana", type: "fruit" },
				{ name: "apple", type: "fruit" },
				{ name: "carrot", type: "vegetable" },
				{ name: "broccoli", type: "vegetable" },
			],
			["type", "name"],
			["asc", "desc"],
		);
		expect(result).toEqual([
			{ name: "banana", type: "fruit" },
			{ name: "apple", type: "fruit" },
			{ name: "carrot", type: "vegetable" },
			{ name: "broccoli", type: "vegetable" },
		]);
	});

	it("returns an empty array for an empty array input", () => {
		const result = orderBy([], ["name"], ["asc"]);
		expect(result).toEqual([]);
	});

	it("handles null/undefined values and proceeds to next key", () => {
		const result = orderBy(
			[
				{ name: "banana", type: null },
				{ name: "apple", type: null },
				{ name: "carrot", type: "vegetable" },
				{ name: "broccoli", type: "vegetable" },
			],
			["type", "name"],
			["asc", "asc"],
		);
		expect(result).toEqual([
			{ name: "apple", type: null },
			{ name: "banana", type: null },
			{ name: "broccoli", type: "vegetable" },
			{ name: "carrot", type: "vegetable" },
		]);
	});

	it("handles null values with descending order", () => {
		const result = orderBy(
			[
				{ name: "apple", priority: null },
				{ name: "banana", priority: 2 },
				{ name: "carrot", priority: 1 },
				{ name: "broccoli", priority: null },
			],
			["priority", "name"],
			["desc", "asc"],
		);
		expect(result).toEqual([
			{ name: "banana", priority: 2 },
			{ name: "carrot", priority: 1 },
			{ name: "apple", priority: null },
			{ name: "broccoli", priority: null },
		]);
	});

	it("throws error when keys and orders length don't match", () => {
		expect(() =>
			orderBy([{ name: "test", priority: 1 }], ["name", "priority"], ["asc"]),
		).toThrow("The number of keys must match the number of orders");
	});
});
