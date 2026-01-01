import type { Lang } from "@cvix/i18n/astro";

/**
 * Interface for category filtering criteria
 */
export interface CategoryCriteria {
	lang?: Lang;
	title?: string;
	orderMin?: number;
	orderMax?: number;
}
