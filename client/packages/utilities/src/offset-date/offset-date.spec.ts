import { describe, expect, it } from "vitest";
import offsetDate from "./offset-date.ts";

describe("offsetDate", () => {
	it("returns the current date when no offset is provided", () => {
		const today = new Date().toISOString().split("T")[0];
		expect(offsetDate()).toBe(today);
	});

	it("returns the correct date when a positive offset is provided", () => {
		// Use a fixed date to avoid end-of-month issues
		const baseDate = new Date("2025-07-15");
		const expected = new Date(baseDate);
		expected.setMonth(expected.getMonth() + 2);
		const expectedDate = expected.toISOString().split("T")[0];
		expect(offsetDate(2, baseDate)).toBe(expectedDate);
	});

	it("returns the correct date when a negative offset is provided", () => {
		// Use a fixed date to avoid end-of-month issues
		const baseDate = new Date("2025-07-15");
		const expected = new Date(baseDate);
		expected.setMonth(expected.getMonth() - 3);
		const expectedDate = expected.toISOString().split("T")[0];
		expect(offsetDate(-3, baseDate)).toBe(expectedDate);
	});

	it("handles year change correctly when offset is positive", () => {
		const date = new Date("2023-11-01");
		date.setMonth(date.getMonth() + 2);
		const expectedDate = date.toISOString().split("T")[0];
		expect(offsetDate(2, new Date("2023-11-01"))).toBe(expectedDate);
	});

	it("handles year change correctly when offset is negative", () => {
		const date = new Date("2023-01-01");
		date.setMonth(date.getMonth() - 2);
		const expectedDate = date.toISOString().split("T")[0];
		expect(offsetDate(-2, new Date("2023-01-01"))).toBe(expectedDate);
	});

	it("handles leap year correctly", () => {
		const date = new Date("2024-02-29");
		date.setMonth(date.getMonth() + 1);
		const expectedDate = date.toISOString().split("T")[0];
		expect(offsetDate(1, new Date("2024-02-29"))).toBe(expectedDate);
	});

	it("handles end-of-month day adjustment", () => {
		const date = new Date("2024-01-31");
		// When adding one month to Jan 31, it should adjust to Feb 29 (leap year)
		const result = offsetDate(1, date);
		expect(result).toBe("2024-02-29");
	});

	it("handles month-end adjustment with non-leap year", () => {
		const date = new Date("2023-01-31");
		// When adding one month to Jan 31 (non-leap), it should adjust to Feb 28
		const result = offsetDate(1, date);
		expect(result).toBe("2023-02-28");
	});
});
