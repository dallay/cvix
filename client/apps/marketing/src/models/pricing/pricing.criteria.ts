import type { Lang } from "@cvix/i18n/astro";

/**
 * Interface for pricing plan filtering criteria
 */
export interface PricingCriteria {
	lang?: Lang;
	title?: string;
	maxPrice?: number;
	minPrice?: number;
	interval?: "month" | "year";
	highlighted?: boolean;
	includeDrafts?: boolean;
}
