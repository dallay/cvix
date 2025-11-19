import {
	isValidPhoneNumber,
	parsePhoneNumberWithError,
} from "libphonenumber-js";
import { type Ref, ref, watch } from "vue";

/**
 * Validation rule function
 */
export type ValidationRule = (value: string) => string | null;

/**
 * Validation options for a field
 */
export interface FieldValidationOptions {
	/**
	 * Whether the field is required
	 */
	required?: boolean;
	/**
	 * Custom validation rules to apply
	 */
	rules?: ValidationRule[];
	/**
	 * Whether to validate on blur (default: true)
	 */
	validateOnBlur?: boolean;
}

/**
 * Result of field validation
 */
export interface FieldValidation {
	/**
	 * Whether the field is valid
	 */
	isValid: Ref<boolean>;
	/**
	 * Validation error message (null if valid)
	 */
	error: Ref<string | null>;
	/**
	 * Validate the field value
	 */
	validate: (value: string) => void;
	/**
	 * Clear the validation error
	 */
	clearError: () => void;
}

/**
 * Composable for field-level validation
 *
 * @example
 * ```typescript
 * const emailValidation = useFieldValidation({
 *   required: true,
 *   rules: [validateEmail],
 * });
 *
 * <Input
 *   v-model="email"
 *   @blur="emailValidation.validate(email)"
 * />
 * <span v-if="emailValidation.error.value">
 *   {{ emailValidation.error.value }}
 * </span>
 * ```
 */
export function useFieldValidation(
	fieldRef: Ref<string>,
	options: FieldValidationOptions = {},
): FieldValidation {
	const { required = false, rules = [] } = options;

	const isValid = ref(true);
	const error = ref<string | null>(null);

	function validate(value: string): void {
		// Check required
		if (required && !value?.trim()) {
			isValid.value = false;
			error.value = "This field is required";
			return;
		}

		// Apply custom rules
		for (const rule of rules) {
			const ruleError = rule(value);
			if (ruleError) {
				isValid.value = false;
				error.value = ruleError;
				return;
			}
		}

		// All validations passed
		isValid.value = true;
		error.value = null;
	}

	function clearError(): void {
		isValid.value = true;
		error.value = null;
	}

	// Watch field value to auto-clear errors when user starts typing
	watch(fieldRef, () => {
		if (error.value) {
			clearError();
		}
	});

	return {
		isValid,
		error,
		validate,
		clearError,
	};
}

/**
 * Validate email format (basic validation only - verify on backend for production use)
 *
 * This performs basic format validation to catch obvious mistakes but does not:
 * - Validate all RFC 5322 compliant email formats
 * - Handle internationalized domain names (IDN)
 * - Check for disposable email domains
 * - Verify the email actually exists
 *
 * For production use, always perform server-side validation and consider
 * sending a confirmation email to verify ownership.
 */
export function validateEmail(email: string): string | null {
	if (!email) {
		return null;
	}

	// Basic format validation - does not catch all edge cases
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	if (!emailRegex.test(email)) {
		return "Please enter a valid email address";
	}

	return null;
}

/**
 * Validate phone number using libphonenumber-js
 */
export function validatePhone(phone: string): string | null {
	if (!phone) {
		return null;
	}

	try {
		if (!isValidPhoneNumber(phone)) {
			return "Please enter a valid phone number";
		}
		return null;
	} catch {
		return "Please enter a valid phone number";
	}
}

/**
 * Normalize phone number to E.164 format
 */
export function normalizePhone(phone: string): string {
	if (!phone) {
		return phone;
	}

	try {
		const phoneNumber = parsePhoneNumberWithError(phone);
		if (phoneNumber) {
			return phoneNumber.format("E.164");
		}
	} catch {
		// Return original if parsing fails
	}

	return phone;
}

/**
 * Validate URL format
 */
export function validateUrl(url: string): string | null {
	if (!url) {
		return null;
	}

	try {
		const urlObj = new URL(url);
		if (!urlObj.protocol.startsWith("http")) {
			return "URL must start with http:// or https://";
		}
		return null;
	} catch {
		return "Please enter a valid URL";
	}
}

/**
 * Validate date range (endDate >= startDate)
 */
export function validateDateRange(
	startDate: string,
	endDate: string,
): string | null {
	if (!startDate || !endDate) {
		return null;
	}

	const start = new Date(startDate);
	const end = new Date(endDate);

	// Check if dates are valid
	if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
		return "Please enter valid dates";
	}

	if (end < start) {
		return "End date must be after or equal to start date";
	}

	return null;
}
