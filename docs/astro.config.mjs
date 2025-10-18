// @ts-check

import starlight from "@astrojs/starlight";
import { defineConfig } from "astro/config";
import mermaid from "astro-mermaid";

// https://astro.build/config
export default defineConfig({
	integrations: [
		mermaid({
			theme: "forest",
			autoTheme: true,
		}),
		starlight({
			title: "Loomify",
			logo: {
				light: "./src/assets/light-isotype.svg",
				dark: "./src/assets/dark-isotype.svg",
			},
			social: [
				{
					icon: "github",
					label: "GitHub",
					href: "https://github.com/yacosta738/saas-template",
				},
				{
					icon: "linkedin",
					label: "LinkedIn",
					href: "https://www.linkedin.com/in/yacosta738/",
				},
			],
			sidebar: [
				{
					label: "🧭 Overview",
					autogenerate: { directory: "overview" },
				},
				{
					label: "🚀 Quick Start Guide",
					autogenerate: { directory: "quick-start" },
				},
				{
					label: "💻 Developer Guide",
					autogenerate: { directory: "developer-guide" },
				},
				{
					label: "⚙️ Configuration",
					autogenerate: { directory: "configuration" },
				},
				{
					label: "📜 Conventions",
					autogenerate: { directory: "conventions" },
				},
				{
					label: "🤝 Contributing",
					link: "/contributing",
				},
				{
					label: "📈 Changelog",
					link: "/changelog",
				},
			],
		}),
	],
});
