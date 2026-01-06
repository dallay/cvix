import { CVIX_API_URL, CVIX_BLOG_URL, CVIX_WEBAPP_URL } from "@cvix/lib";
import { envField } from "astro/config";

export default {
	AHREFS_KEY: envField.string({
		context: "client",
		access: "public",
		default: "",
	}),
	CVIX_API_URL: envField.string({
		context: "client",
		access: "public",
		optional: true,
		default: CVIX_API_URL,
	}),
	CVIX_WEBAPP_URL: envField.string({
		context: "client",
		access: "public",
		default: CVIX_WEBAPP_URL,
	}),
	CVIX_BLOG_URL: envField.string({
		context: "client",
		access: "public",
		default: CVIX_BLOG_URL,
	}),
	HCAPTCHA_SITE_KEY: envField.string({
		context: "client",
		access: "public",
		optional: true,
	}),
};
