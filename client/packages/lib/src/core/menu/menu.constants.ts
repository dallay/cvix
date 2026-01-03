import type { Lang } from "@cvix/i18n/astro";
import { CVIX_BLOG_URL, CVIX_DOCS_URL } from "../../consts/config";
import type { MenuItem } from "./menu.type";

export const headerMenuItems: MenuItem[] = [
	{
		type: "link",
		href: "/",
		translationKey: "header.nav.home",
		condition: true,
	},
	{
		type: "link",
		href: CVIX_BLOG_URL,
		translationKey: "header.nav.blog",
		condition: true,
	},
	{
		type: "link",
		href: "/products",
		translationKey: "header.nav.products",
		condition: true,
	},
	{
		type: "link",
		href: "/#price",
		translationKey: "header.nav.pricing",
		condition: true,
	},
	{
		type: "dropdown",
		children: [
			{
				type: "link",
				href: CVIX_DOCS_URL,
				translationKey: "header.nav.resources.docs",
				condition: true,
			},
			{
				type: "link",
				href: "/#faq",
				translationKey: "header.nav.resources.faq",
				condition: true,
			},
		],
		translationKey: "header.nav.resources",
		condition: true,
	},
];

/**
 * Build the list of footer navigation items with translation and accessibility keys.
 *
 * @param lang - Locale code used to construct language-specific links (e.g., RSS feed)
 * @returns An array of `MenuItem` objects representing footer links and their metadata
 */
export function footerNavLinks(lang: Lang): MenuItem[] {
	return [
		{
			type: "link",
			href: "/about/",
			translationKey: "footer.about",
			ariaLabelKey: "footer.aria.about",
			condition: true,
		},
		{
			type: "link",
			href: "/contact/",
			translationKey: "footer.contact",
			ariaLabelKey: "footer.aria.contact",
			condition: true,
		},
		{
			type: "link",
			href: "/support/",
			translationKey: "footer.donate",
			ariaLabelKey: "footer.aria.donate",
			condition: true,
		},
		// SEO Satellite Pages
		{
			type: "link",
			href: "/ats-resume-builder/",
			translationKey: "footer.atsResumeBuilder",
			ariaLabelKey: "footer.aria.atsResumeBuilder",
			condition: true,
		},
		{
			type: "link",
			href: "/latex-resume-builder/",
			translationKey: "footer.latexResumeBuilder",
			ariaLabelKey: "footer.aria.latexResumeBuilder",
			condition: true,
		},
		{
			type: "link",
			href: "/tech-resume-template/",
			translationKey: "footer.techResumeTemplate",
			ariaLabelKey: "footer.aria.techResumeTemplate",
			condition: true,
		},
		{
			type: "link",
			href: `${CVIX_BLOG_URL}/${lang}/rss.xml`,
			translationKey: "footer.rss",
			ariaLabelKey: "footer.aria.rss",
			target: "_blank",
			condition: true,
		},
		{
			type: "link",
			href: "/privacy-policy/",
			translationKey: "footer.privacyPolicy",
			ariaLabelKey: "footer.aria.privacyPolicy",
			condition: true,
		},
		{
			type: "link",
			href: "/terms-of-use/",
			translationKey: "footer.termsOfUse",
			ariaLabelKey: "footer.aria.termsOfUse",
			condition: true,
		},
	];
}