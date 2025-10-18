/**
 * Represents a pricing plan entity in the system.
 * @interface PricingPlan
 * @property {string} id - The unique identifier of the pricing plan
 * @property {string} title - The title/name of the pricing plan
 * @property {string} description - The description of the pricing plan
 * @property {number} price - The price of the plan
 * @property {"month" | "year"} interval - The billing interval
 * @property {Feature[]} features - The features included in the plan
 * @property {boolean} highlighted - Whether this plan should be highlighted
 * @property {number} order - The display order of the plan
 *
 * @example
 * // English pricing plan
 * const englishPlan: PricingPlan = {
 *   id: "en/basic",
 *   title: "Basic",
 *   description: "For small teams getting started.",
 *   price: 0,
 *   interval: "month",
 *   features: [
 *     { text: "Up to 500 subscribers" },
 *     { text: "Basic features" }
 *   ],
 *   highlighted: false,
 *   order: 0
 * };
 *
 * // Spanish pricing plan
 * const spanishPlan: PricingPlan = {
 *   id: "es/basico",
 *   title: "Básico",
 *   description: "Para equipos pequeños que están empezando.",
 *   price: 0,
 *   interval: "month",
 *   features: [
 *     { text: "Hasta 500 suscriptores" },
 *     { text: "Funcionalidades básicas" }
 *   ],
 *   highlighted: false,
 *   order: 0
 * };
 */

/**
 * Represents a feature in a pricing plan
 * @interface Feature
 * @property {string} text - The feature description
 * @property {string} [value] - Optional value or specification for the feature
 */
export interface Feature {
	text: string;
	value?: string;
}

export default interface PricingPlan {
	id: string;
	title: string;
	description: string;
	price: number;
	interval: "month" | "year";
	features: Feature[];
	highlighted: boolean;
	order: number;
	draft?: boolean;
}
