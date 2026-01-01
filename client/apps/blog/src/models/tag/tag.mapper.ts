/**
 * Maps collection entries of tags to Tag objects
 * @module TagMapper
 */

import type { CollectionEntry } from "astro:content";
import type Tag from "./tag.model";

/**
 * Converts a single tag collection entry to a Tag object
 * @param {CollectionEntry<"tags">} tagData - The tag collection entry to convert
 * @returns {Tag} A Tag object with id and title
 */
export function toTag(tagData: CollectionEntry<"tags">): Tag {
	return {
		id: tagData.id,
		title: tagData.data.title,
	};
}
/**
 * Converts an array of tag collection entries to Tag objects
 * @param {CollectionEntry<"tags">[]} tags - Array of tag collection entries
 * @returns {Promise<Tag[]>} Promise that resolves to an array of Tag objects
 */
export async function toTags(tags: CollectionEntry<"tags">[]): Promise<Tag[]> {
	return tags.map(toTag);
}
