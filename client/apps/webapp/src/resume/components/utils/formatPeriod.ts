/**
 * Formats a date period (start date to end date or present) in a localized format.
 *
 * @param startDate - The start date in ISO format (YYYY-MM-DD)
 * @param endDate - Optional end date in ISO format (YYYY-MM-DD)
 * @param locale - Optional locale string (defaults to browser locale)
 * @param presentText - Optional text to display for "present" (defaults to "Present")
 * @returns Formatted period string (e.g., "Jan 2025 - Dec 2025" or "Jan 2025 - Present")
 */
export const formatPeriod = (
	startDate?: string,
	endDate?: string,
	locale?: string,
	presentText = "Present",
): string => {
	if (!startDate) return "";

	// Validate date format (YYYY-MM-DD)
	const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
	if (!dateRegex.test(startDate) || (endDate && !dateRegex.test(endDate))) {
		console.warn("formatPeriod: Invalid date format. Expected YYYY-MM-DD");
		return "";
	}

	const currentLocale =
		locale ||
		(typeof navigator !== "undefined" ? navigator.language : undefined) ||
		"en-US";
	const start = new Date(startDate).toLocaleDateString(currentLocale, {
		year: "numeric",
		month: "short",
	});

	if (!endDate) {
		return `${start} - ${presentText}`;
	}

	const end = new Date(endDate).toLocaleDateString(currentLocale, {
		year: "numeric",
		month: "short",
	});

	return `${start} - ${end}`;
};
