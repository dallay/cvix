import {
	defineCollection,
	reference,
	type SchemaContext,
	z,
} from "astro:content";
import { glob } from "astro/loaders";

const articles = defineCollection({
	loader: glob({ pattern: "**/[^_]*.{md,mdx}", base: "./src/data/blog" }),
	schema: ({ image }: SchemaContext) =>
		z.object({
			title: z.string(),
			description: z.string(),
			date: z.coerce.date(),
			lastModified: z.coerce
				.date()
				.optional()
				.default(() => new Date()),
			cover: image().optional(),
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

const pricing = defineCollection({
	loader: glob({
		pattern: "**/**/*.{json,yml,yaml}",
		base: "./src/data/pricing",
	}),
	schema: z.object({
		title: z.string(),
		description: z.string(),
		price: z.number(),
		interval: z.enum(["month", "year"]).default("month"),
		features: z.array(
			z.object({
				text: z.string(),
				value: z.string().optional(),
			}),
		),
		highlighted: z.boolean().optional().default(false),
		order: z.number().optional().default(0),
		draft: z.boolean().optional().default(false),
	}),
});

const faq = defineCollection({
	loader: glob({ pattern: "**/[^_]*.md", base: "./src/data/faq" }),
	schema: z.object({
		question: z.string(),
		date: z.coerce.date(),
	}),
});

export const collections = {
	articles,
	tags,
	categories,
	authors,
	pricing,
	faq,
};
