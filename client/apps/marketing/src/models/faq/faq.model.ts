import type { CollectionEntry } from "astro:content";

/**
 * Represents a Frequently Asked Question (FAQ) entry.
 * @interface FAQ
 * @property {string} id - Unique identifier for the FAQ entry
 * @property {string} question - The question text of the FAQ entry
 * @property {string} answer - The answer text for the FAQ question
 */
export default interface FAQ {
	id: string;
	question: string;
	date: Date;
	answer: string;
	entry?: CollectionEntry<"faq">;
}
