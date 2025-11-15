import { cva, type VariantProps } from "class-variance-authority";

export { default as CTAEmail } from "./CTAEmail.vue";

export const ctaEmailVariants = cva(
	"flex flex-col items-center gap-4 sm:flex-row sm:gap-2",
	{
		variants: {
			size: {
				sm: "mb-6 gap-3 sm:gap-1.5",
				default: "mb-10 gap-4 sm:gap-2",
				lg: "mb-12 gap-6 sm:gap-3",
			},
			alignment: {
				left: "sm:justify-start",
				center: "sm:justify-center",
				right: "sm:justify-end",
			},
		},
		defaultVariants: {
			size: "default",
			alignment: "center",
		},
	},
);

export const ctaEmailInputVariants = cva(
	"w-full rounded-md border px-4 py-3 shadow-sm transition-all focus:ring-2 focus:ring-offset-2 text-gray-900 dark:text-gray-100 placeholder:text-gray-500 dark:placeholder:text-gray-400 bg-white dark:bg-gray-800",
	{
		variants: {
			variant: {
				default:
					"border-gray-300 dark:border-gray-600 focus:border-blue-500 focus:ring-blue-500 dark:focus:border-blue-400 dark:focus:ring-blue-400",
				primary:
					"border-primary/30 dark:border-primary/40 focus:border-primary focus:ring-primary/20 dark:focus:border-primary dark:focus:ring-primary/30",
				secondary:
					"border-gray-200 dark:border-gray-700 focus:border-gray-400 focus:ring-gray-300 dark:focus:border-gray-500 dark:focus:ring-gray-400",
			},
			size: {
				sm: "px-3 py-2 text-sm",
				default: "px-4 py-3",
				lg: "px-5 py-4 text-lg",
			},
		},
		defaultVariants: {
			variant: "default",
			size: "default",
		},
	},
);

export const ctaEmailButtonVariants = cva(
	"w-full whitespace-nowrap rounded-md font-medium shadow-sm transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 sm:w-auto",
	{
		variants: {
			variant: {
				default:
					"bg-primary text-primary-foreground shadow-xs hover:bg-primary/90",
				destructive:
					"bg-destructive text-white shadow-xs hover:bg-destructive/90 focus-visible:ring-destructive/20 dark:focus-visible:ring-destructive/40 dark:bg-destructive/60",
				outline:
					"border bg-background shadow-xs hover:bg-accent hover:text-accent-foreground dark:bg-input/30 dark:border-input dark:hover:bg-input/50",
				secondary:
					"bg-secondary text-secondary-foreground shadow-xs hover:bg-secondary/80",
				ghost:
					"hover:bg-accent hover:text-accent-foreground dark:hover:bg-accent/50",
				link: "text-primary underline-offset-4 hover:underline",
			},
			size: {
				sm: "px-6 py-2 text-sm",
				default: "px-8 py-3 text-base",
				lg: "px-10 py-4 text-lg",
			},
		},
		defaultVariants: {
			variant: "default",
			size: "default",
		},
	},
);

export type CTAEmailVariants = VariantProps<typeof ctaEmailVariants>;
export type CTAEmailInputVariants = VariantProps<typeof ctaEmailInputVariants>;
export type CTAEmailButtonVariants = VariantProps<
	typeof ctaEmailButtonVariants
>;
