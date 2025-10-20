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
		label: "Users",
		icon: "users",
		to: "/users",
		children: [
			{
				label: "All Users",
				to: "/users",
			},
			{
				label: "Invite User",
				to: "/users/invite",
			},
		],
	},
	{
		label: "Settings",
		icon: "settings",
		to: "/settings",
	},
	{
		label: "Docs",
		icon: "book",
		to: "https://docs.example.com",
		external: true,
	},
];

// Exported function to get navigation items
export function getNavigationItems(): NavigationItem[] {
	return navigationItems;
}

// Export type for use in components
export type { NavigationItem };
