import type { Lang } from "@cvix/i18n/astro";

/**
 * Interface for tag filtering criteria
 */
export interface TagCriteria {
	lang?: Lang;
	title?: string;
}
