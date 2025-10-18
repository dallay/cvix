/**
 * Offsets a date by a specified number of months and returns the ISO date string (YYYY-MM-DD).
 *
 * @param {number} offsetMonths - The number of months to offset, defaults to 0.
 * @param {Date} date - The date to offset, defaults to the current date.
 * @returns {string} - ISO date string (YYYY-MM-DD) of the offset date.
 *
 * @example
 * // Returns the ISO date string for 3 months from now
 * const futureDate = offsetDate(3);
 *
 * // Returns the ISO date string for 2 months before a specific date
 * const pastDate = offsetDate(-2, new Date('2023-05-15'));
 */
export default function offsetDate(
	offsetMonths = 0,
	date = new Date(),
): string {
	// Create a new Date object to avoid modifying the input
	const newDate = new Date(date);

	// Get the current day of month before modifying
	const currentDay = newDate.getDate();

	// Set to the 1st of the month to avoid invalid date issues
	newDate.setDate(1);

	// Add the months
	newDate.setMonth(newDate.getMonth() + offsetMonths);

	// Try to restore the original day (will automatically adjust if invalid)
	newDate.setDate(
		Math.min(
			currentDay,
			new Date(newDate.getFullYear(), newDate.getMonth() + 1, 0).getDate(),
		),
	);

	return newDate.toISOString().split("T")[0];
}
