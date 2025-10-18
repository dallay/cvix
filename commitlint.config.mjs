/**
 * commitlint.config.mjs
 * @ref http://commitlint.js.org/
 * @type {import('@commitlint/types').UserConfig}
 */
export default {
	extends: ["@commitlint/config-conventional"],
	rules: {
		"header-max-length": [2, "always", 120],
		"body-max-line-length": [2, "always", 220],
	},
};
