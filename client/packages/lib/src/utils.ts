import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Utility function to merge Tailwind CSS classes with proper conflict resolution.
 * Combines clsx for conditional class names and tailwind-merge for deduplication.
 *
 * @example
 * cn("px-4 py-2", "px-6") // => "py-2 px-6"
 * cn("text-red-500", condition && "text-blue-500") // => conditional class
 */
export function cn(...inputs: ClassValue[]): string {
	return twMerge(clsx(inputs));
}
