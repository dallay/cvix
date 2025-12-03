import { describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import {
	normalizePhone,
	useFieldValidation,
	validateDateRange,
	validateEmail,
	validatePhone,
	validateUrl,
} from "./useFieldValidation";

describe("useFieldValidation", () => {
	describe("basic validation", () => {
		it("should initialize with valid state", () => {
			const fieldRef = ref("");
			const validation = useFieldValidation(fieldRef);

			expect(validation.isValid.value).toBe(true);
			expect(validation.error.value).toBeNull();
		});

		it("should validate required field with empty value", () => {
			const fieldRef = ref("");
			const validation = useFieldValidation(fieldRef, { required: true });

			validation.validate("");

			expect(validation.isValid.value).toBe(false);
			expect(validation.error.value).toBe("This field is required");
		});

		it("should validate required field with whitespace only", () => {
			const fieldRef = ref("   ");
			const validation = useFieldValidation(fieldRef, { required: true });

			validation.validate("   ");

			expect(validation.isValid.value).toBe(false);
			expect(validation.error.value).toBe("This field is required");
		});

		it("should pass required validation with valid value", () => {
			const fieldRef = ref("test");
			const validation = useFieldValidation(fieldRef, { required: true });

			validation.validate("test");

			expect(validation.isValid.value).toBe(true);
			expect(validation.error.value).toBeNull();
		});

		it("should clear error on clearError call", () => {
			const fieldRef = ref("");
			const validation = useFieldValidation(fieldRef, { required: true });

			validation.validate("");
			expect(validation.error.value).toBe("This field is required");

			validation.clearError();
			expect(validation.isValid.value).toBe(true);
			expect(validation.error.value).toBeNull();
		});

		it("should auto-clear error when field value changes", async () => {
			const fieldRef = ref("");
			const validation = useFieldValidation(fieldRef, { required: true });

			validation.validate("");
			expect(validation.error.value).toBe("This field is required");

			fieldRef.value = "new value";
			await nextTick();

			expect(validation.isValid.value).toBe(true);
			expect(validation.error.value).toBeNull();
		});
	});

	describe("custom rules", () => {
		it("should apply custom validation rules", () => {
			const fieldRef = ref("short");
			const minLengthRule = (value: string) =>
				value.length < 10 ? "Value must be at least 10 characters" : null;

			const validation = useFieldValidation(fieldRef, {
				rules: [minLengthRule],
			});

			validation.validate("short");

			expect(validation.isValid.value).toBe(false);
			expect(validation.error.value).toBe(
				"Value must be at least 10 characters",
			);
		});

		it("should pass when custom rules are satisfied", () => {
			const fieldRef = ref("this is long enough");
			const minLengthRule = (value: string) =>
				value.length < 10 ? "Value must be at least 10 characters" : null;

			const validation = useFieldValidation(fieldRef, {
				rules: [minLengthRule],
			});

			validation.validate("this is long enough");

			expect(validation.isValid.value).toBe(true);
			expect(validation.error.value).toBeNull();
		});

		it("should stop at first failing rule", () => {
			const fieldRef = ref("ab");
			const rule1 = (value: string) => (value.length < 3 ? "Too short" : null);
			const rule2 = (value: string) =>
				value.length < 5 ? "Still too short" : null;

			const validation = useFieldValidation(fieldRef, {
				rules: [rule1, rule2],
			});

			validation.validate("ab");

			expect(validation.error.value).toBe("Too short");
		});

		it("should check all rules when first passes", () => {
			const fieldRef = ref("abcd");
			const rule1 = (value: string) => (value.length < 3 ? "Too short" : null);
			const rule2 = (value: string) =>
				value.length < 5 ? "Still too short" : null;

			const validation = useFieldValidation(fieldRef, {
				rules: [rule1, rule2],
			});

			validation.validate("abcd");

			expect(validation.error.value).toBe("Still too short");
		});

		it("should check required before custom rules", () => {
			const fieldRef = ref("");
			const customRule = vi.fn(() => null);

			const validation = useFieldValidation(fieldRef, {
				required: true,
				rules: [customRule],
			});

			validation.validate("");

			expect(validation.error.value).toBe("This field is required");
			expect(customRule).not.toHaveBeenCalled();
		});
	});
});

describe("validateEmail", () => {
	it("should return null for empty email", () => {
		expect(validateEmail("")).toBeNull();
	});

	it("should return null for valid email addresses", () => {
		const validEmails = [
			"test@example.com",
			"user.name@domain.org",
			"user+tag@example.co.uk",
			"123@numbers.com",
		];

		validEmails.forEach((email) => {
			expect(validateEmail(email)).toBeNull();
		});
	});

	it("should return error for invalid email addresses", () => {
		const invalidEmails = [
			"notanemail",
			"@nodomain.com",
			"noat.domain.com",
			"spaces in@email.com",
			"email@",
		];

		invalidEmails.forEach((email) => {
			expect(validateEmail(email)).toBe("Please enter a valid email address");
		});
	});
});

describe("validatePhone", () => {
	it("should return null for empty phone", () => {
		expect(validatePhone("")).toBeNull();
	});

	it("should return null for valid phone numbers", () => {
		// libphonenumber-js requires full international format with country code
		const validPhones = [
			"+1 650 253 0000", // US with country code and spaces
			"+14155551234", // US compact
			"+44 20 7946 0958", // UK
			"+34 612 345 678", // Spain
			"+49 30 12345678", // Germany
		];

		validPhones.forEach((phone) => {
			expect(validatePhone(phone)).toBeNull();
		});
	});

	it("should return error for invalid phone numbers", () => {
		const invalidPhones = [
			"notaphone",
			"12345",
			"abc-def-ghij",
			"555-555-5555", // Missing country code
		];

		invalidPhones.forEach((phone) => {
			expect(validatePhone(phone)).toBe("Please enter a valid phone number");
		});
	});
});

describe("normalizePhone", () => {
	it("should return empty string for empty input", () => {
		expect(normalizePhone("")).toBe("");
	});

	it("should normalize valid phone to E.164 format", () => {
		expect(normalizePhone("+1 (555) 555-5555")).toBe("+15555555555");
		expect(normalizePhone("+44 20 7946 0958")).toBe("+442079460958");
	});

	it("should return original for invalid phone", () => {
		expect(normalizePhone("notaphone")).toBe("notaphone");
	});
});

describe("validateUrl", () => {
	it("should return null for empty URL", () => {
		expect(validateUrl("")).toBeNull();
	});

	it("should return null for valid URLs", () => {
		const validUrls = [
			"https://example.com",
			"http://subdomain.example.org/path",
			"https://example.com:8080/path?query=value",
			"http://localhost:3000",
		];

		validUrls.forEach((url) => {
			expect(validateUrl(url)).toBeNull();
		});
	});

	it("should return error for URLs without http/https", () => {
		expect(validateUrl("ftp://example.com")).toBe(
			"URL must start with http:// or https://",
		);
	});

	it("should return error for invalid URLs", () => {
		const invalidUrls = ["notaurl", "example.com", "://missing-protocol.com"];

		invalidUrls.forEach((url) => {
			expect(validateUrl(url)).toBe("Please enter a valid URL");
		});
	});
});

describe("validateDateRange", () => {
	it("should return null when startDate is empty", () => {
		expect(validateDateRange("", "2023-12-31")).toBeNull();
	});

	it("should return null when endDate is empty", () => {
		expect(validateDateRange("2023-01-01", "")).toBeNull();
	});

	it("should return null for valid date range", () => {
		expect(validateDateRange("2023-01-01", "2023-12-31")).toBeNull();
	});

	it("should return null when dates are equal", () => {
		expect(validateDateRange("2023-06-15", "2023-06-15")).toBeNull();
	});

	it("should return error when end date is before start date", () => {
		expect(validateDateRange("2023-12-31", "2023-01-01")).toBe(
			"End date must be after or equal to start date",
		);
	});

	it("should return error for invalid dates", () => {
		expect(validateDateRange("invalid", "2023-12-31")).toBe(
			"Please enter valid dates",
		);
		expect(validateDateRange("2023-01-01", "invalid")).toBe(
			"Please enter valid dates",
		);
	});
});
