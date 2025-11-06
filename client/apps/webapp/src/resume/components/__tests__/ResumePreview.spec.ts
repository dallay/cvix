import { describe, expect, it } from "vitest";
import { formatPeriod } from "../utils/formatPeriod";

describe("formatPeriod", () => {
	it("handles invalid date format gracefully", () => {
		const result = formatPeriod("invalid-date");
		expect(result).toBe("Invalid Date - Present"); // Current behavior
	});

	it("handles malformed ISO dates", () => {
		const result = formatPeriod("2025-13-45");
		expect(result).toBe("Invalid Date - Present"); // Current behavior
	});
	it("formats a period with start and end dates in the default locale", () => {
		const startDate = "2025-01-01";
		const endDate = "2025-12-31";
		const result = formatPeriod(startDate, endDate);
		// Use a regex to match the expected structure, e.g., "Jan 2025 - Dec 2025"
		expect(result).toMatch(/^\w{3,} \d{4} - \w{3,} \d{4}$/);
	});

	it("formats a period with only a start date in the default locale", () => {
		const startDate = "2025-01-01";
		const result = formatPeriod(startDate);
		expect(result).toBe("Jan 2025 - Present");
	});

	it("formats a period with a custom locale", () => {
		const startDate = "2025-01-01";
		const endDate = "2025-12-31";
		const result = formatPeriod(startDate, endDate, "es-ES");
		expect(result).toBe("ene 2025 - dic 2025");
	});

	it("formats a period with a custom present text", () => {
		const startDate = "2025-01-01";
		const result = formatPeriod(startDate, undefined, "en-US", "Presente");
		expect(result).toBe("Jan 2025 - Presente");
	});

	it("returns empty string when no start date is provided", () => {
		const result = formatPeriod();
		expect(result).toBe("");
	});
});
