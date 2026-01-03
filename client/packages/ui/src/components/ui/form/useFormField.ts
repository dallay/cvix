import {
	FieldContextKey,
	useFieldError,
	useIsFieldDirty,
	useIsFieldTouched,
	useIsFieldValid,
} from "vee-validate";
import { inject } from "vue";
import { FORM_ITEM_INJECTION_KEY } from "./injectionKeys.js";

/**
 * Exposes identifiers and reactive validation state for the current form field.
 *
 * @returns An object containing:
 * - `id`: the injected form item id
 * - `name`: the field name from the field context
 * - `formItemId`: id for the form item element (`${id}-form-item`)
 * - `formDescriptionId`: id for the form item description element (`${id}-form-item-description`)
 * - `formMessageId`: id for the form item message element (`${id}-form-item-message`)
 * - `valid`: reactive validation state for the field
 * - `isDirty`: reactive dirty state for the field
 * - `isTouched`: reactive touched state for the field
 * - `error`: reactive field error message (if any)
 *
 * @throws Error If called outside a `<FormField>` (throws "useFormField should be used within <FormField>")
 */
export function useFormField() {
	const fieldContext = inject(FieldContextKey);
	const fieldItemContext = inject(FORM_ITEM_INJECTION_KEY);

	if (!fieldContext)
		throw new Error("useFormField should be used within <FormField>");

	const { name } = fieldContext;
	const id = fieldItemContext;

	const fieldState = {
		valid: useIsFieldValid(name),
		isDirty: useIsFieldDirty(name),
		isTouched: useIsFieldTouched(name),
		error: useFieldError(name),
	};

	return {
		id,
		name,
		formItemId: `${id}-form-item`,
		formDescriptionId: `${id}-form-item-description`,
		formMessageId: `${id}-form-item-message`,
		...fieldState,
	};
}