export { default as ChartCrosshair } from "./ChartCrosshair.vue";
export { default as ChartLegend } from "./ChartLegend.vue";
export { default as ChartSingleTooltip } from "./ChartSingleTooltip.vue";
export { default as ChartTooltip } from "./ChartTooltip.vue";

/**
 * Generate an array of HSL color strings that use theme CSS variables with progressively decreasing opacity.
 *
 * The array contains `count` entries: primary colors first (using --vis-primary-color), then secondary colors (using --vis-secondary-color). Opacity for each group decreases linearly from 1.
 *
 * @param count - The total number of colors to generate (defaults to 3)
 * @returns An array of `count` color strings in the form `hsl(var(--vis-...-color) / <opacity>)`
 */
export function defaultColors(count = 3) {
	const quotient = Math.floor(count / 2);
	const remainder = count % 2;

	const primaryCount = quotient + remainder;
	const secondaryCount = quotient;
	return [
		...Array.from(
			{ length: primaryCount },
			(_, i) => `hsl(var(--vis-primary-color) / ${1 - (1 / primaryCount) * i})`,
		),
		...Array.from(
			{ length: secondaryCount },
			(_, i) =>
				`hsl(var(--vis-secondary-color) / ${1 - (1 / secondaryCount) * i})`,
		),
	];
}

export * from "./interface.js";