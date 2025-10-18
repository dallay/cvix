import type { Lang } from "@/i18n";

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
