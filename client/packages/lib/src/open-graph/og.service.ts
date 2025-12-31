import { readFile } from "node:fs/promises";
import { DEFAULT_LOCALE } from "@cvix/i18n";
import { type Lang, useTranslations } from "@cvix/i18n/astro";
import { SITE_TITLE } from "@cvix/lib";
import satori, { type SatoriOptions } from "satori";
import sharp from "sharp";
import { Template } from "./og.template";

const currentLang = (process.env.LANG as Lang) || DEFAULT_LOCALE;
const t = useTranslations(currentLang);
const siteTitle = t(SITE_TITLE);

// getOgImagePath moved to og.utils.ts

/**
 * generate opengraph image with satori and return a buffer
 *
 * @param title Title text
 * @param date Date to display
 * @param author Author name
 * @param category Category name
 * @param tags Array of tags
 * @param lang Language code
 */
const generateOgImage = async (
	title: string = siteTitle,
	author?: string,
	category?: string,
	tags?: string[],
	date: Date = new Date(),
	lang: string = DEFAULT_LOCALE,
): Promise<Buffer> => {
	try {
		const options: SatoriOptions = {
			width: 1200,
			height: 630,
			embedFont: true,
			fonts: [
				{
					name: "JetBrainsMono",
					data: await readFile("./src/assets/font/JetBrainsMono-Bold.ttf"),
					weight: 600,
					style: "normal",
				},
				{
					name: "JetBrains Mono",
					data: await readFile("./src/assets/font/JetBrainsMono-Regular.ttf"),
					weight: 400,
					style: "normal",
				},
				{
					name: "PlusJakartaSans",
					data: await readFile("./src/assets/font/PlusJakartaSans-Bold.ttf"),
					weight: 900,
					style: "normal",
				},
			],
		};

		const svg = await satori(
			Template({
				title,
				author,
				category,
				tags,
				date,
				lang,
			}),
			options,
		);

		const sharpSvg = Buffer.from(svg);
		const buffer = await sharp(sharpSvg).toBuffer();

		return buffer;
	} catch (error) {
		console.error("Failed to generate OG image:", error);
		throw new Error(
			`Failed to generate OG image: ${error instanceof Error ? error.message : String(error)}`,
		);
	}
};

export default generateOgImage;
