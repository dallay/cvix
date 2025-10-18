module.exports = {
	overrides: [
		{
			files: ["**/*.scss"],
			customSyntax: "postcss-scss",
		},
	],
	extends: [
		"stylelint-config-standard",
		"stylelint-config-html/html",
		"stylelint-config-html/xml",
		"stylelint-config-html/vue",
		"stylelint-config-html/astro",
	],
	rules: {
		"selector-pseudo-class-no-unknown": [
			true,
			{
				ignorePseudoClasses: ["global"],
			},
		],
		"import-notation": null,
		"at-rule-no-unknown": [
			true,
			{
				ignoreAtRules: [
					/** tailwindcss v4 */
					"theme",
					"source",
					"utility",
					"variant",
					"custom-variant",
					"plugin",
					/** tailwindcss v3 */
					"tailwind",
					"apply",
					"layer",
					"config",
					/** tailwindcss v1, v2 */
					"variants",
					"responsive",
					"screen",
					/** existing custom rules */
					"unocss",
					"ProseMirror",
					"use",
					"reference",
				],
			},
		],
		"selector-class-pattern": null,
		"function-no-unknown": [
			true,
			{
				ignoreFunctions: ["theme", "reference"],
			},
		],
	},
};
