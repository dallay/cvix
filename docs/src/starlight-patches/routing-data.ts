// @ts-nocheck

import type { RenderResult } from "astro:content";
import { getNewestCommitDate } from "virtual:starlight/git-info";
import project from "virtual:starlight/project-context";
import config from "virtual:starlight/user-config";
import type { APIContext, MarkdownHeading } from "astro";
import { getCollectionPathFromRoot } from "../collection";
import { formatPath } from "../format-path";
import { generateToC } from "../generateToC";
import { getHead } from "../head";
import { BuiltInDefaultLocale } from "../i18n";
import { getPrevNextLinks, getSidebar } from "../navigation";
import { ensureTrailingSlash } from "../path";
import { getRouteBySlugParam } from "../routing";
import { useTranslations } from "../translations";
import type { Route, StarlightDocsEntry, StarlightRouteData } from "./types";

export interface PageProps extends Route {
	headings: MarkdownHeading[];
}

export type RouteDataContext = Pick<APIContext, "generator" | "site" | "url">;

export async function getRoute(context: APIContext): Promise<Route> {
	return (
		("slug" in context.params && getRouteBySlugParam(context.params.slug)) ||
		(await get404Route(context.locals))
	);
}

export function useRouteData(
	context: APIContext,
	route: Route,
	{ Content, headings }: RenderResult,
): StarlightRouteData {
	const routeData = generateRouteData({
		props: { ...route, headings },
		context,
	});
	return { ...routeData, Content };
}

export function generateRouteData({
	props,
	context,
}: {
	props: PageProps;
	context: RouteDataContext;
}): StarlightRouteData {
	const { entry, locale, lang } = props;
	const sidebar = getSidebar(context.url.pathname, locale);
	const siteTitle = getSiteTitle(lang);
	return {
		...props,
		siteTitle,
		siteTitleHref: getSiteTitleHref(locale),
		sidebar,
		hasSidebar: entry.data.template !== "splash",
		pagination: getPrevNextLinks(sidebar, config.pagination, entry.data),
		toc: getToC(props),
		lastUpdated: getLastUpdated(props),
		editUrl: getEditUrl(props),
		head: getHead(props, context, siteTitle),
	};
}

export function getToC({ entry, lang, headings }: PageProps) {
	const tocConfig =
		entry.data.template === "splash"
			? false
			: entry.data.tableOfContents !== undefined
				? entry.data.tableOfContents
				: config.tableOfContents;
	if (!tocConfig) return;
	const t = useTranslations(lang);
	return {
		...tocConfig,
		items: generateToC(headings, {
			...tocConfig,
			title: t("tableOfContents.overview"),
		}),
	};
}

function getLastUpdated({ entry }: PageProps): Date | undefined {
	const { lastUpdated: frontmatterLastUpdated } = entry.data;
	const { lastUpdated: configLastUpdated } = config;

	if (frontmatterLastUpdated ?? configLastUpdated) {
		try {
			return frontmatterLastUpdated instanceof Date
				? frontmatterLastUpdated
				: getNewestCommitDate(entry.filePath);
		} catch {
			return undefined;
		}
	}

	return undefined;
}

function getEditUrl({ entry }: PageProps): URL | undefined {
	const { editUrl } = entry.data;
	if (editUrl === false) return;

	let url: string | undefined;
	if (typeof editUrl === "string") {
		url = editUrl;
	} else if (config.editLink.baseUrl) {
		url = ensureTrailingSlash(config.editLink.baseUrl) + entry.filePath;
	}
	return url ? new URL(url) : undefined;
}

export function getSiteTitle(lang: string): string {
	const defaultLang = config.defaultLocale.lang as string;
	if (lang && config.title[lang]) {
		return config.title[lang];
	}
	return config.title[defaultLang] as string;
}

export function getSiteTitleHref(locale: string | undefined): string {
	return formatPath(locale || "/");
}

async function get404Route(locals: App.Locals): Promise<Route> {
	const { lang = BuiltInDefaultLocale.lang, dir = BuiltInDefaultLocale.dir } =
		config.defaultLocale || {};
	let locale = config.defaultLocale?.locale;
	if (locale === "root") locale = undefined;

	const entryMeta = { dir, lang, locale };

	console.info("[starlight] Using patched get404Route override");

	const fallbackEntry: StarlightDocsEntry = {
		slug: "404",
		id: "404",
		body: "",
		collection: "docs",
		data: {
			title: "404",
			template: "splash",
			editUrl: false,
			head: [],
			hero: { tagline: locals.t("404.text"), actions: [] },
			pagefind: false,
			sidebar: { hidden: false, attrs: {} },
			draft: false,
		},
		filePath: `${getCollectionPathFromRoot("docs", project)}/404.md`,
	};

	const entry = fallbackEntry;
	return { ...entryMeta, entryMeta, entry, id: entry.id, slug: entry.slug };
}
