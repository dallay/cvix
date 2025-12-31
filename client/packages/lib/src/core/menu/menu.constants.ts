import type { Lang } from "@cvix/i18n/astro";
import { BASE_DOCS_URL, BLOG_URL } from "../../consts/config";
import type { MenuItem } from "./menu.type.ts";

export const headerMenuItems: MenuItem[] = [
	{
		type: "link",
		href: "/",
		translationKey: "header.nav.home",
		condition: true,
	},
	{
		type: "link",
		href: BLOG_URL,
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
				href: BASE_DOCS_URL,
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

// Navigation links array with translation keys and conditions
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
		{
			type: "link",
			href: `${BLOG_URL}/${lang}/rss.xml`,
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
