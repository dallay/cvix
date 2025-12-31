import { DEFAULT_LOCALE } from "@cvix/i18n";
import { type Lang, useTranslations } from "@cvix/i18n/astro";
import { SITE_TITLE } from "../consts/config";

const currentLang =
	(typeof process !== "undefined" && (process.env.LANG as Lang)) ||
	DEFAULT_LOCALE;
const t = useTranslations(currentLang);
const siteTitle = t(SITE_TITLE);

/**
 * generate filename / path for generated OG images
 *
 * @param filename filename in asset folder
 * @returns
 */
export const getOgImagePath = (filename: string = siteTitle) => {
	let path = filename;

	if (path.startsWith("/")) path = path.substring(1);

	if (path.endsWith("/")) path = path.substring(0, path.length - 1);

	if (path === "") path = siteTitle;

	return `./og/${path}.png`;
};
