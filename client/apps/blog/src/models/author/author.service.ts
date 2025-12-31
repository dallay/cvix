/**
 * Service module for handling Author-related operations.
 * @module AuthorService
 */

import { getCollection } from "astro:content";
import { isSupportedLocale } from "@cvix/i18n";
import { getArticles } from "@/models/article";
import type { ArticleCriteria } from "@/models/article/article.criteria";
import { parseEntityId } from "@/utils/collection.entity";
import type { AuthorCriteria } from "./author.criteria";
import { toAuthors } from "./author.mapper";
import type Author from "./author.model";

/**
 * Retrieves authors from the content collection with filtering options
 * @async
 * @param {AuthorCriteria} criteria - Criteria for filtering authors
 * @returns {Promise<Author[]>} A promise that resolves to an array of filtered Author objects
 */
export const getAuthors = async (
	criteria?: AuthorCriteria,
): Promise<Author[]> => {
	const { lang, name, location } = criteria || {};

	const authors = await getCollection("authors", ({ id, data }) => {
		// Filter by language if provided
		if (lang) {
			const authorLang = parseEntityId(id).lang;
			if (authorLang !== lang) return false;
		}

		// Filter by name if provided
		if (name && !data.name.toLowerCase().includes(name.toLowerCase())) {
			return false;
		}

		// Filter by email if provided
		if (
			criteria?.email &&
			!data.email?.toLowerCase().includes(criteria.email.toLowerCase())
		) {
			return false;
		}

		// Filter by role if provided
		if (
			criteria?.role &&
			!data.role?.toLowerCase().includes(criteria.role.toLowerCase())
		) {
			return false;
		}

		// Filter by location if provided
		if (
			location &&
			!data.location?.toLowerCase().includes(location.toLowerCase())
		) {
			return false;
		}

		return true;
	});

	return toAuthors(authors);
};

/**
 * Retrieves a specific author by their ID.
 * @async
 * @param {string} id - The unique identifier of the author to retrieve.
 * @returns {Promise<Author | undefined>} A promise that resolves to an Author object if found, undefined otherwise.
 */
export const getAuthorById = async (
	id: string,
): Promise<Author | undefined> => {
	const authors = await getAuthors();
	return authors.find((author) => author.id === id);
};

/**
 * Count articles written by an author
 * @param authorId - author id (full collection id or cleaned id)
 * @param lang - optional locale filter
 */
export const countArticlesByAuthor = async (
	authorId: string,
	lang?: string,
): Promise<number> => {
	// normalize cleaned id (match how article.author.id is stored)
	const cleaned = authorId.toLowerCase();
	const criteria: Partial<ArticleCriteria> = {};

	// Only set lang if it's a supported locale
	if (lang && isSupportedLocale(lang)) {
		criteria.lang = lang;
	}

	const articles = await getArticles(criteria as ArticleCriteria);

	return articles.filter((a) => {
		if (!a.author?.id) return false;
		const aid = a.author.id.toLowerCase();
		return aid === cleaned || aid.endsWith(`/${cleaned}`);
	}).length;
};
