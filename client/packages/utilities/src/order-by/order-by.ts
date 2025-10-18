/**
 * Type definition for order. It can be either 'asc' for ascending order or 'desc' for descending order.
 */
export type Order = "asc" | "desc";

/**
 * Sorts an array of objects based on multiple keys and orders.
 *
 * @param array - The array to sort.
 * @param keys - The keys of the object to sort by.
 * @param orders - The order for each key. It should be either 'asc' for ascending order or 'desc' for descending order.
 * @returns A new array sorted by the specified keys and orders.
 *
 * @example
 * orderBy([{name: 'banana', type: 'fruit'}, {name: 'apple', type: 'fruit'}], ['type', 'name'], ['asc', 'desc']);
 * // Returns: [{name: 'banana', type: 'fruit'}, {name: 'apple', type: 'fruit'}]
 */
export function orderBy<T>(
	array: T[],
	keys: (keyof T)[],
	orders: Order[],
): T[] {
	if (keys.length !== orders.length) {
		throw new Error("The number of keys must match the number of orders");
	}
	return [...array].sort((a, b) => {
		for (let i = 0; i < keys.length; i++) {
			const aValue = a[keys[i]];
			const bValue = b[keys[i]];

			// Handle null/undefined values
			if (aValue === null || aValue === undefined) {
				return orders[i] === "asc" ? -1 : 1;
			}
			if (bValue === null || bValue === undefined) {
				return orders[i] === "asc" ? 1 : -1;
			}

			if (aValue > bValue) return orders[i] === "asc" ? 1 : -1;
			if (aValue < bValue) return orders[i] === "asc" ? -1 : 1;
		}
		return 0;
	});
}
