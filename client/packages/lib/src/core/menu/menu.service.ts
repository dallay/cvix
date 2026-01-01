import type { MenuItem } from "./menu.type";

/**
 * Filters menu items based on their condition property
 * @param items Array of menu items
 * @returns Filtered array of menu items where condition is not false
 */
export function filterMenuItems(items: MenuItem[]): MenuItem[] {
	if (!items || items.length === 0) {
		return [];
	}
	return items.filter((item) => item.condition !== false);
}
