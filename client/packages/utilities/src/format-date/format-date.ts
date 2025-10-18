import { DateFormatter } from "@internationalized/date";

/**
 * Formats a date into a localized long date string.
 *
 * @param {Date|string|undefined} date - The date to format.
 * @param {string} [locale] - The locale to use for formatting (defaults to browser’s locale or "en-US").
 * @returns {string} The formatted date string or empty string if the date is invalid.
 *
 * @example
 * formatDate(new Date(2023, 3, 12));
 * // → "April 12, 2023" (varies by locale)
 *
 * @example
 * formatDate(new Date(2023, 3, 12), "de-DE");
 * // → "12. April 2023"
 */
export default function formatDate(
	date: Date | string | undefined,
	locale?: string,
): string {
	// Determine locale, supporting both browser and Node.js environments
	const userLocale =
		locale ||
		(typeof navigator !== "undefined" ? navigator.language : undefined) ||
		"en-US";

	const df = new DateFormatter(userLocale, {
		dateStyle: "long",
	});

	if (!date) return "";

	const dateToFormat = new Date(date);

	if (Number.isNaN(dateToFormat.getTime())) {
		return "";
	}

	try {
		return df.format(dateToFormat);
	} catch (_error) {
		// Fallback to default locale if formatting fails
		if (userLocale !== "en-US") {
			const fallbackFormatter = new DateFormatter("en-US", {
				dateStyle: "long",
			});
			return fallbackFormatter.format(dateToFormat);
		}
		return "";
	}
}
