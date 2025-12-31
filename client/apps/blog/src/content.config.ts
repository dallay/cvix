import { defineCollection, reference, z } from "astro:content";
import { glob } from "astro/loaders";

const articles = defineCollection({
	loader: glob({ pattern: "**/[^_]*.{md,mdx}", base: "./src/data/blog" }),
	schema: z.object({
		title: z.string(),
		description: z.string(),
		date: z.coerce.date(),
		lastModified: z.coerce
			.date()
			.optional()
			.default(() => new Date()),
		cover: z.string().optional(),
		author: reference("authors"),
		tags: z.array(reference("tags")),
		category: reference("categories"),
		draft: z.boolean().optional().default(false),
		featured: z.boolean().optional().default(false),
	}),
});

const tags = defineCollection({
	loader: glob({ pattern: "**/[^_]*.md", base: "./src/data/tags" }),
	schema: z.object({
		title: z.string(),
	}),
});

const categories = defineCollection({
	loader: glob({ pattern: "**/[^_]*.md", base: "./src/data/categories" }),
	schema: z.object({
		title: z.string(),
		order: z.number().optional(),
	}),
});

const authors = defineCollection({
	loader: glob({ pattern: "**/[^_]*.json", base: "./src/data/authors" }),
	schema: z.object({
		name: z.string(),
		email: z.string(),
		avatar: z.string(),
		bio: z.string(),
		role: z.string(),
		location: z.string().optional(),
		socials: z
			.array(
				z.object({
					name: z.string(),
					url: z.string(),
					icon: z.string(),
				}),
			)
			.optional()
			.default([]),
	}),
});

export const collections = {
	articles,
	tags,
	categories,
	authors,
};
