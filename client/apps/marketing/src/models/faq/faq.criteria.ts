import type { Lang } from "@cvix/i18n/astro";

/**
 * Interface for FAQ filtering criteria
 */
export interface FaqCriteria {
	lang?: Lang;
	question?: string;
	date?: Date;
	answer?: string;
}
