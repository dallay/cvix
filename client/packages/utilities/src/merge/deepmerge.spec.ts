import { describe, expect, it } from "vitest";
import { deepmerge } from "./deepmerge";

describe("deepmerge", () => {
	it("merges two simple objects", () => {
		const a = { foo: 1 };
		const b = { bar: 2 };
		const result = deepmerge(a, b);
		expect(result).toEqual({ foo: 1, bar: 2 });
	});

	it("deep merges nested objects", () => {
		const a = { user: { name: "Ana" } };
		const b = { user: { age: 30 } };
		const result = deepmerge(a, b);
		expect(result).toEqual({ user: { name: "Ana", age: 30 } });
	});

	it("overrides primitives with new values", () => {
		const a = { value: 10 };
		const b = { value: 20 };
		const result = deepmerge(a, b);
		expect(result).toEqual({ value: 20 });
	});

	it("concatenates arrays by default", () => {
		const a = { list: [1, 2] };
		const b = { list: [3, 4] };
		const result = deepmerge(a, b);
		expect(result).toEqual({ list: [1, 2, 3, 4] });
	});

	it("can override array merge behavior", () => {
		const a = { list: [1, 2] };
		const b = { list: [3, 4] };
		const result = deepmerge(a, b, {
			arrayMerge: (_, source) => source,
		});
		expect(result).toEqual({ list: [3, 4] });
	});

	it("merges symbol keys", () => {
		const sym = Symbol("key");
		const a = { [sym]: 1 };
		const b = { [sym]: 2 };
		const result = deepmerge(a, b);
		expect(result[sym]).toBe(2);
	});

	it("merges multiple objects with deepmerge.all", () => {
		const objects = [{ a: 1 }, { b: 2 }, { c: { d: 3 } }];
		const result = deepmerge.all(objects);
		expect(result).toEqual({ a: 1, b: 2, c: { d: 3 } });
	});

	it("returns source when types mismatch", () => {
		const result = deepmerge({ val: 1 }, [1, 2]);
		expect(result).toEqual([1, 2]);
	});

	it("clones objects when clone is true", () => {
		const a = { nested: { x: 1 } };
		const b = { nested: { y: 2 } };
		const result = deepmerge(a, b, { clone: true });
		expect(result).toEqual({ nested: { x: 1, y: 2 } });
		expect((result as { nested: object }).nested).not.toBe(a.nested);
		expect((result as { nested: object }).nested).not.toBe(b.nested);
	});

	it("skips unsafe prototype properties", () => {
		const a = Object.create(null);
		const b = { constructor: { malicious: true } };
		const result = deepmerge(a, b);
		expect(result).not.toHaveProperty("constructor");
	});
});
