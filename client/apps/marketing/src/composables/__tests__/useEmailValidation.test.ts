import { beforeEach, describe, expect, it, vi } from "vitest";
import { useEmailValidation } from "../useEmailValidation";

// Mock the i18n module
vi.mock("@/i18n", () => ({
	useTranslations: vi.fn(() => {
		// Return a function that acts as the translation function
		return vi.fn((key: string) => {
			const translations: Record<string, string> = {
				"form.email.required": "Email address is required.",
				"form.email.invalid":
					"Invalid email address. Please enter a valid email.",
			};
			return translations[key] || key;
		});
	}),
}));

describe("useEmailValidation", () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it("should create validation schema with required email", () => {
		const { validationSchema } = useEmailValidation({
			lang: "en",
			required: true,
		});

		expect(validationSchema.value).toBeDefined();
	});

	it("should validate a correct email address", async () => {
		const { validateEmail } = useEmailValidation({ lang: "en" });

		const result = await validateEmail("test@example.com");

		expect(result.isValid).toBe(true);
		expect(result.error).toBeUndefined();
	});

	it("should reject invalid email addresses", async () => {
		const { validateEmail } = useEmailValidation({ lang: "en" });

		const invalidEmails = [
			"invalid-email",
			"test@",
			"@example.com",
			"test..test@example.com",
			"",
		];

		for (const email of invalidEmails) {
			const result = await validateEmail(email);
			expect(result.isValid).toBe(false);
			expect(result.error).toBeDefined();
		}
	});

	it("should provide email suggestions", () => {
		const { getEmailSuggestions } = useEmailValidation({ lang: "en" });

		const suggestions = getEmailSuggestions("john");
		expect(suggestions).toHaveLength(0); // No @ symbol

		const suggestionsWithAt = getEmailSuggestions("john@");
		expect(suggestionsWithAt).toHaveLength(5);
		expect(suggestionsWithAt[0]).toBe("john@gmail.com");
	});

	it("should handle loading state during validation", async () => {
		vi.useFakeTimers();
		const { validateEmail, isValidating } = useEmailValidation({ lang: "en" });

		expect(isValidating.value).toBe(false);

		// Start validation but don't await immediately
		const validationPromise = validateEmail("test@example.com");

		// Advance timers immediately
		vi.runAllTimers();

		// Check if validation is in progress
		expect(isValidating.value).toBe(true);

		// Wait for validation to complete
		const result = await validationPromise;

		expect(result.isValid).toBe(true);
		expect(isValidating.value).toBe(false);

		vi.useRealTimers();
	});

	it("should use custom error message when provided", async () => {
		const customMessage = "Custom error message";
		const { validateEmail } = useEmailValidation({
			lang: "en",
			customMessage,
		});

		const result = await validateEmail("invalid-email");

		expect(result.isValid).toBe(false);
		expect(result.error).toBe(customMessage);
	});
});
