/**
 * Creates an array of numbers (positive and/or negative) progressing from start up to, but not including, end.
 * If end is not specified it’s set to start with start then set to 0.
 * If end is less than start a zero-length range is created unless a negative step is specified.
 *
 * @param start – The start of the range.
 * @param end – The end of the range.
 * @param step – The value to increment or decrement by.
 * @returns Returns a new range array.
 *
 * @example
 * range(1, 5, 1); // Returns: [1, 2, 3, 4]
 * range(5, 1, -1); // Returns: [5, 4, 3, 2]
 * range(5, 1, 1); // Returns: []
 * range(1, 5, -1); // Returns: []
 * range(5, 5, 1); // Returns: []
 * range(0, 5, 1); // Returns: [0, 1, 2, 3, 4]
 */
export function range(start: number, end?: number, step = 1): number[] {
	let actualStart = start;
	let actualEnd = end;

	if (actualEnd === undefined) {
		actualEnd = actualStart;
		actualStart = 0;
	}

	const length = Math.max(Math.ceil((actualEnd - actualStart) / step), 0);
	return Array.from({ length }, (_, i) => actualStart + i * step);
}
