/**
 * Service module for handling PricingPlan-related operations.
 * @module PricingService
 */

import { getCollection } from "astro:content";
import { parseEntityId } from "@/utils/collection.entity";
import type { PricingCriteria } from "./pricing.criteria";
import { toPricingPlans } from "./pricing.mapper";
import type PricingPlan from "./pricing.model";

/**
 * Retrieves pricing plans from the content collection with filtering options
 * @async
 * @param {PricingCriteria} criteria - Criteria for filtering pricing plans
 * @returns {Promise<PricingPlan[]>} A promise that resolves to an array of filtered PricingPlan objects
 */
export const getPricingPlans = async (
	criteria?: PricingCriteria,
): Promise<PricingPlan[]> => {
	const {
		lang,
		title,
		maxPrice,
		minPrice,
		interval,
		highlighted,
		includeDrafts = false,
	} = criteria || {};

	const pricingPlans = await getCollection("pricing", ({ id, data }) => {
		// Always check draft status unless includeDrafts is true
		if (!includeDrafts && data.draft) return false;

		// Filter by language if provided
		if (lang) {
			const planLang = parseEntityId(id).lang;
			if (planLang !== lang) return false;
		}

		// Filter by title if provided
		if (title && !data.title.toLowerCase().includes(title.toLowerCase())) {
			return false;
		}

		// Filter by price range if provided
		if (maxPrice !== undefined && data.price > maxPrice) {
			return false;
		}

		if (minPrice !== undefined && data.price < minPrice) {
			return false;
		}

		// Filter by interval if provided
		if (interval && data.interval !== interval) {
			return false;
		}

		// Filter by highlighted status if provided
		if (highlighted !== undefined && data.highlighted !== highlighted) {
			return false;
		}

		return true;
	});

	// Sort by order field
	const sortedPlans = pricingPlans.sort(
		(a, b) => (a.data.order || 0) - (b.data.order || 0),
	);

	return toPricingPlans(sortedPlans);
};

/**
 * Retrieves a specific pricing plan by its ID.
 * @async
 * @param {string} id - The unique identifier of the pricing plan to retrieve.
 * @returns {Promise<PricingPlan | undefined>} A promise that resolves to a PricingPlan object if found, undefined otherwise.
 */
export const getPricingPlanById = async (
	id: string,
): Promise<PricingPlan | undefined> => {
	const pricingPlans = await getPricingPlans();
	return pricingPlans.find((plan) => plan.id === id);
};

/**
 * Retrieves all highlighted pricing plans
 * @async
 * @returns {Promise<PricingPlan[]>} A promise that resolves to an array of highlighted PricingPlan objects
 */
export const getHighlightedPricingPlans = async (): Promise<PricingPlan[]> => {
	return getPricingPlans({ highlighted: true });
};
