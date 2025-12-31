import type { Lang } from "@cvix/i18n/astro";

/**
 * Interface for author filtering criteria
 */
export interface AuthorCriteria {
	lang?: Lang;
	name?: string;
	email?: string;
	role?: string;
	location?: string;
	hasArticles?: boolean;
}
