import type { CollectionEntry } from "astro:content";
import type PricingPlan from "./pricing.model";

/**
 * Maps pricing collection data to PricingPlan model
 * @param pricingData - The pricing collection entry from Astro content
 * @returns A PricingPlan model object with all required properties
 */
export function toPricingPlan(
	pricingData: CollectionEntry<"pricing">,
): PricingPlan {
	if (!pricingData?.data) {
		throw new Error("Invalid pricing data: data object is missing");
	}

	const { data } = pricingData;
	if (!data.title) {
		throw new Error("Invalid pricing data: title is required");
	}

	return {
		id: pricingData.id,
		title: data.title,
		description: data.description ?? "", // Provide default for optional fields
		price: data.price ?? 0, // Provide default for optional fields
		interval: data.interval ?? "month", // Provide default for optional fields
		features: data.features ?? [], // Provide default for optional fields
		highlighted: data.highlighted ?? false, // Provide default for optional fields
		order: data.order ?? 0, // Provide default for optional fields
		draft: data.draft ?? false, // Provide default for optional fields
	};
}

/**
 * Maps an array of pricing collection data to an array of PricingPlan models
 * @param pricingPlans - Array of pricing collection entries from Astro content
 * @returns Array of PricingPlan model objects
 */
export function toPricingPlans(
	pricingPlans: CollectionEntry<"pricing">[],
): PricingPlan[] {
	return pricingPlans.map(toPricingPlan);
}
