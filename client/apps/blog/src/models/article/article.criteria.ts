import type { Lang } from "@cvix/i18n/astro";

/**
 * Interface for article filtering criteria
 */
export interface ArticleCriteria {
	lang?: Lang;
	includeDrafts?: boolean;
	author?: string | string[];
	tags?: string | string[];
	category?: string | string[];
	featured?: boolean;
}
