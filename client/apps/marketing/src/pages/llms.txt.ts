import { CVIX_MARKETING_URL } from "astro:env/client";
import { LOCALES, SUPPORTED_LOCALES } from "@cvix/i18n";
import type { Lang } from "@cvix/i18n/astro";
import type { APIRoute } from "astro";

interface MdxPageModule {
	frontmatter: {
		title?: string;
		description?: string;
	};
}

function hasTitleAndDescription(
	frontmatter: MdxPageModule["frontmatter"],
): frontmatter is Required<MdxPageModule["frontmatter"]> {
	return (
		typeof frontmatter.title === "string" &&
		typeof frontmatter.description === "string"
	);
}

function generatePageList(
	pages: [string, MdxPageModule][],
	lang: Lang,
	siteUrl: URL,
): string[] {
	return pages
		.filter(([path]) => path.startsWith(`./${lang}/`))
		.map(([path, page]) => ({
			frontmatter: page.frontmatter,
			slug: path.split("/").pop()?.replace(".mdx", "") ?? "",
		}))
		.filter(({ slug }) => slug.length > 0)
		.filter(({ frontmatter }) => hasTitleAndDescription(frontmatter))
		.map(({ frontmatter, slug }) => {
			const pageUrl = new URL(`${lang}/${slug}`, siteUrl).toString();
			return `- [${frontmatter.title}](${pageUrl}) – ${frontmatter.description}`;
		});
}

export const GET: APIRoute = async () => {
	// Validate CVIX_MARKETING_URL env at request time with clear error message
	const siteEnv = CVIX_MARKETING_URL;
	if (!siteEnv) {
		throw new Error(
			"CVIX_MARKETING_URL environment variable is required for llms.txt generation. Please set CVIX_MARKETING_URL in your .env file.",
		);
	}
	const siteUrl = new URL(siteEnv);

	const pagesRecord = await import.meta.glob("./(en|es)/*.mdx", {
		eager: true,
	});
	const pages = Object.entries(pagesRecord).filter(
		(entry): entry is [string, MdxPageModule] =>
			typeof entry[1] === "object" &&
			entry[1] !== null &&
			"frontmatter" in entry[1],
	);

	const lines = [
		"# ProFileTailors",
		"> ProFileTailors is a resume generator that helps you create a professional resume in minutes.",
		"",
	];

	// Generate sections for each supported locale using locale config
	for (const lang of SUPPORTED_LOCALES) {
		const localePages = generatePageList(pages, lang, siteUrl);
		if (localePages.length > 0) {
			const localeLabel = LOCALES[lang].label;
			lines.push(`## ${localeLabel} Pages`, ...localePages, "");
		}
	}

	const body = lines.join("\n");

	return new Response(body, {
		headers: { "Content-Type": "text/plain; charset=utf-8" },
	});
};
