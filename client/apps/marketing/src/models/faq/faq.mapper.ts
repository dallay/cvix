import type { CollectionEntry } from "astro:content";
import type FAQ from "./faq.model";

/**
 * Maps FAQ collection data to FAQ model
 * @param faqData - The FAQ collection entry from Astro content
 * @returns An FAQ model object with all required properties
 */

export function toFAQ(faqData: CollectionEntry<"faq">): FAQ {
	if (!faqData?.data) {
		throw new Error("Invalid FAQ data: data object is missing");
	}

	const { data, body } = faqData;
	if (!data.question) {
		throw new Error("Invalid FAQ data: question is required");
	}
	if (!data.date) {
		throw new Error("Invalid FAQ data: date is required");
	}

	if (!body) {
		throw new Error("Invalid FAQ data: answer is required");
	}

	return {
		id: faqData.id,
		question: data.question,
		answer: body,
		date: data.date,
		entry: faqData,
	};
}

/**
 * Maps an array of FAQ collection data to an array of FAQ models
 * @param faqs - Array of FAQ collection entries from Astro content
 * @returns Array of FAQ model objects
 */
export function toFAQs(faqs: CollectionEntry<"faq">[]): FAQ[] {
	return faqs.map(toFAQ);
}
