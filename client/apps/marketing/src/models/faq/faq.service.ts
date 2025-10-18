/**
 * Service module for handling FAQ-related operations.
 * @module FaqService
 */

import { getCollection } from "astro:content";
import { parseEntityId } from "@/utils/collection.entity";
import type { FaqCriteria } from "./faq.criteria";
import { toFAQs } from "./faq.mapper";
import type FAQ from "./faq.model";

/**
 * Retrieves FAQs from the content collection with filtering options
 * @async
 * @param {FaqCriteria} criteria - Criteria for filtering FAQs
 * @returns {Promise<FAQ[]>} A promise that resolves to an array of filtered FAQ objects
 */
export const getFAQs = async (criteria?: FaqCriteria): Promise<FAQ[]> => {
	const { lang, question, answer, date } = criteria || {};

	const faqs = await getCollection("faq", ({ id, data, body }) => {
		// Filter by language if provided
		if (lang) {
			const faqLang = parseEntityId(id).lang;
			if (faqLang !== lang) return false;
		}

		// Filter by question if provided
		if (
			question &&
			!data.question.toLowerCase().includes(question.toLowerCase())
		) {
			return false;
		}

		// Filter by answer if provided
		if (answer && !body?.toLowerCase().includes(answer.toLowerCase())) {
			return false;
		}

		// Filter by date if provided
		if (date && data.date !== date) {
			return false;
		}

		return true;
	});

	return toFAQs(faqs);
};

/**
 * Retrieves a specific FAQ by its ID.
 * @async
 * @param {string} id - The unique identifier of the FAQ to retrieve.
 * @returns {Promise<FAQ | undefined>} A promise that resolves to an FAQ object if found, undefined otherwise.
 */
export const getFAQById = async (id: string): Promise<FAQ | undefined> => {
	const faqs = await getFAQs();
	return faqs.find((faq) => faq.id === id);
};
