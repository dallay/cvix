import { computed, ref } from "vue";
import * as z from "zod";
import { type Lang, useTranslations } from "@/i18n";

export interface EmailValidationOptions {
	lang?: Lang;
	required?: boolean;
	customMessage?: string;
}

export function useEmailValidation(options: EmailValidationOptions = {}) {
	const { lang = "en", required = true, customMessage } = options;
	const t = useTranslations(lang);

	const isValidating = ref(false);
	const lastValidatedEmail = ref<string>("");

	// Create validation schema based on options
	const validationSchema = computed(() => {
		let schema = z.email({
			message: customMessage || t("form.email.invalid"),
		});

		if (required) {
			schema = schema.min(1, { message: t("form.email.required") });
		}

		return schema;
	});

	// Email validation function
	const validateEmail = async (
		email: string,
	): Promise<{ isValid: boolean; error?: string }> => {
		isValidating.value = true;
		lastValidatedEmail.value = email;

		try {
			await validationSchema.value.parseAsync(email);
			return { isValid: true };
		} catch (error) {
			if (error instanceof z.ZodError) {
				return {
					isValid: false,
					error: error.issues[0]?.message,
				};
			}
			return {
				isValid: false,
				error: t("form.email.invalid"),
			};
		} finally {
			isValidating.value = false;
		}
	};

	// Common email patterns for suggestions
	const getEmailSuggestions = (input: string): string[] => {
		if (!input.includes("@") || input.includes("@.")) return [];

		const [localPart] = input.split("@");
		const commonDomains = [
			"gmail.com",
			"yahoo.com",
			"hotmail.com",
			"outlook.com",
			"icloud.com",
		];

		return commonDomains.map((domain) => `${localPart}@${domain}`);
	};

	return {
		validationSchema,
		isValidating: computed(() => isValidating.value),
		lastValidatedEmail: computed(() => lastValidatedEmail.value),
		validateEmail,
		getEmailSuggestions,
	};
}
