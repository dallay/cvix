import { onUnmounted, ref, watch } from "vue";

export type Theme = "light" | "dark" | "system";

const THEME_STORAGE_KEY = "theme-preference";

// Type guard for Theme
function isValidTheme(value: unknown): value is Theme {
	return value === "light" || value === "dark" || value === "system";
}

// Get the initial theme from localStorage or default to system
function getInitialTheme(): Theme {
	try {
		const stored = localStorage.getItem(THEME_STORAGE_KEY);
		if (isValidTheme(stored)) {
			return stored;
		}
	} catch (error) {
		console.debug("localStorage not available:", error);
	}
	return "system";
}

// Check if system prefers dark mode
function getSystemTheme(): "light" | "dark" {
	if (
		typeof window !== "undefined" &&
		window.matchMedia &&
		window.matchMedia("(prefers-color-scheme: dark)").matches
	) {
		return "dark";
	}
	return "light";
}

// Global state for theme
const theme = ref<Theme>(getInitialTheme());
const resolvedTheme = ref<"light" | "dark">(
	theme.value === "system" ? getSystemTheme() : theme.value,
);

// Apply the theme to the document
function applyTheme(appliedTheme: "light" | "dark") {
	if (typeof document === "undefined") return;

	const root = document.documentElement;
	root.classList.remove("light", "dark");
	root.classList.add(appliedTheme);
	resolvedTheme.value = appliedTheme;
}

// Media query for system theme detection
let mediaQuery: MediaQueryList | null = null;
let mediaQueryListener: ((e: MediaQueryListEvent) => void) | null = null;

// Initialize theme on first load
if (typeof document !== "undefined") {
	applyTheme(resolvedTheme.value);

	// Listen for system theme changes
	mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
	mediaQueryListener = () => {
		if (theme.value === "system") {
			const newSystemTheme = getSystemTheme();
			applyTheme(newSystemTheme);
		}
	};

	// Modern API
	if (mediaQuery.addEventListener) {
		mediaQuery.addEventListener("change", mediaQueryListener);
	}
}

// Watch for theme changes
watch(theme, (newTheme) => {
	try {
		localStorage.setItem(THEME_STORAGE_KEY, newTheme);
	} catch (error) {
		console.warn("Failed to save theme to localStorage:", error);
	}

	const appliedTheme = newTheme === "system" ? getSystemTheme() : newTheme;
	applyTheme(appliedTheme);
});

/**
 * Composable for managing theme state
 */
export function useTheme() {
	const setTheme = (newTheme: Theme) => {
		theme.value = newTheme;
	};

	const toggleTheme = () => {
		if (resolvedTheme.value === "light") {
			setTheme("dark");
		} else {
			setTheme("light");
		}
	};

	// Cleanup media query listener on unmount
	onUnmounted(() => {
		if (mediaQuery && mediaQueryListener) {
			if (mediaQuery.removeEventListener) {
				mediaQuery.removeEventListener("change", mediaQueryListener);
			}
		}
	});

	return {
		theme,
		resolvedTheme,
		setTheme,
		toggleTheme,
	};
}
