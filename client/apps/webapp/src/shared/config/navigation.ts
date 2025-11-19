// Navigation item type
type NavigationItem = {
	label: string;
	icon?: string; // Icon name, e.g., 'home', 'users', etc.
	to: string; // Route path
	children?: NavigationItem[];
	external?: boolean; // For external links
};

// Example navigation items (customize as needed)
const navigationItems: NavigationItem[] = [
	{
		label: "Dashboard",
		icon: "home",
		to: "/dashboard",
	},
	{
		label: "Resumes",
		icon: "analytics",
		to: "/resume",
		children: [
			{ label: "Editor", to: "/resume/editor" },
			{ label: "PDF Generator", to: "/resume/pdf" },
			{ label: "Reports", to: "/dashboard?view=reports" },
		],
	},
	{
		label: "Settings",
		icon: "settings",
		to: "/settings",
		children: [
			{ label: "Storage", to: "/settings?section=storage" },
			{ label: "General", to: "/settings?section=general" },
			{ label: "Privacy", to: "/settings?section=privacy" },
		],
	},
	{
		label: "Support",
		icon: "support",
		to: "https://loomify.io/support",
		external: true,
	},
];

// Exported function to get navigation items
export function getNavigationItems(): NavigationItem[] {
	return navigationItems;
}

// Export type for use in components
export type { NavigationItem };
