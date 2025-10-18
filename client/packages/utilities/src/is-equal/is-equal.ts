/**
 * Performs a deep comparison between two values to determine if they are equivalent.
 *
 * This function works by comparing the values directly if they are not objects.
 * If they are objects, it compares their keys and their corresponding values.
 * If the values of a key are also objects, it recursively calls `isEqual` to compare them.
 *
 * Note: This function is a simplified version and may not cover all edge cases that lodash's `isEqual` function does.
 * For example, it does not handle comparisons for functions, DOM nodes, Maps, Sets, etc.
 * If you need to handle these cases, you might want to consider sticking with lodash's `isEqual` or another library that provides deep equality checks.
 *
 * @param {unknown} value - The first value to compare.
 * @param {unknown} other - The second value to compare.
 * @returns {boolean} - Returns `true` if the values are equivalent, else `false`.
 */
export function isEqual(value: unknown, other: unknown): boolean {
	if (value === other) return true;

	if (
		typeof value !== "object" ||
		value === null ||
		typeof other !== "object" ||
		other === null
	) {
		return false;
	}

	// Check for arrays first
	if (Array.isArray(value) && Array.isArray(other)) {
		if (value.length !== other.length) return false;
		for (let i = 0; i < value.length; i++) {
			if (!isEqual(value[i], other[i])) return false;
		}
		return true;
	}

	// Handle Date objects
	if (value instanceof Date && other instanceof Date) {
		return value.getTime() === other.getTime();
	}

	// Cast regular objects
	const valueRecord = value as Record<string, unknown>;
	const otherRecord = other as Record<string, unknown>;
	const keysA = Object.keys(valueRecord);
	const keysB = Object.keys(otherRecord);

	if (keysA.length !== keysB.length) return false;

	for (const key of keysA) {
		if (!keysB.includes(key)) return false;

		const valueAtKey = valueRecord[key];
		const otherAtKey = otherRecord[key];

		if (
			typeof valueAtKey === "object" &&
			valueAtKey !== null &&
			typeof otherAtKey === "object" &&
			otherAtKey !== null
		) {
			if (!isEqual(valueAtKey, otherAtKey)) return false;
		} else {
			if (valueAtKey !== otherAtKey) return false;
		}
	}

	return true;
}
