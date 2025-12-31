import { defineCollection, z } from "astro:content";
import { glob } from "astro/loaders";

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
	loader: glob({ pattern: "**/[^_]*.json", base: "./src/data/faq" }),
	schema: z.object({
		question: z.string(),
		date: z.coerce.date(),
		body: z.string(),
	}),
});

export const collections = {
	pricing,
	faq,
};
