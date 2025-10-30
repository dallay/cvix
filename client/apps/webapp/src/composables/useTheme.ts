import { ref, watch } from "vue";

export type Theme = "light" | "dark" | "system";

const THEME_STORAGE_KEY = "theme-preference";

// Get the initial theme from localStorage or default to system
function getInitialTheme(): Theme {
	try {
		const stored = localStorage.getItem(THEME_STORAGE_KEY);
		if (stored === "light" || stored === "dark" || stored === "system") {
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

// Initialize theme on first load
if (typeof document !== "undefined") {
	applyTheme(resolvedTheme.value);

	// Listen for system theme changes
	const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
	const handleSystemThemeChange = () => {
		if (theme.value === "system") {
			const newSystemTheme = getSystemTheme();
			applyTheme(newSystemTheme);
		}
	};

	// Modern API
	if (mediaQuery.addEventListener) {
		mediaQuery.addEventListener("change", handleSystemThemeChange);
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

	return {
		theme,
		resolvedTheme,
		setTheme,
		toggleTheme,
	};
}
