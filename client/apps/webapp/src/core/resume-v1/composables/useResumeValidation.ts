import { toTypedSchema } from "@vee-validate/zod";
import { useForm } from "vee-validate";
import type { ResumeFormData } from "@/core/resume-v1/schemas/resumeSchema.ts";
import { resumeSchema } from "@/core/resume-v1/schemas/resumeSchema.ts";

/**
 * Composable for resume form validation using vee-validate and Zod.
 *
 * This composable integrates vee-validate with our Zod schema to provide:
 * - Type-safe form validation
 * - Manual validation on blur (per VUE_CONVENTIONS.md)
 * - Field-level error messages
 * - Form-level validation state
 *
 * Usage:
 * ```ts
 * const { handleSubmit, errors, validateField, values, setFieldValue } = useResumeValidation();
 * ```
 *
 * @returns vee-validate form context with typed values and validation functions
 */
export function useResumeValidation(initialValues?: Partial<ResumeFormData>) {
	const {
		handleSubmit,
		errors,
		values,
		setFieldValue,
		setValues,
		validateField,
		resetForm,
		meta,
	} = useForm<ResumeFormData>({
		validationSchema: toTypedSchema(resumeSchema),
		initialValues: initialValues ?? {
			basics: {
				name: "",
				email: "",
			},
		},
		// Per VUE_CONVENTIONS.md: validateOnMount: false
		validateOnMount: false,
	});

	return {
		handleSubmit,
		errors,
		values,
		setFieldValue,
		setValues,
		validateField,
		resetForm,
		meta,
	};
}
