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
		to: "/resumes",
		children: [
			{ label: "Resume", to: "/resume" },
			{ label: "Reports", to: "/dashboard?view=reports" },
		],
	},
	{
		label: "Team",
		icon: "users",
		to: "/dashboard?panel=team",
		children: [
			{ label: "Members", to: "/dashboard?panel=team&tab=members" },
			{ label: "Roles", to: "/dashboard?panel=team&tab=roles" },
			{ label: "Approvals", to: "/dashboard?panel=team&tab=approvals" },
		],
	},
	{
		label: "Settings",
		icon: "settings",
		to: "/dashboard?panel=settings",
		children: [
			{ label: "General", to: "/dashboard?panel=settings&tab=general" },
			{
				label: "Billing",
				icon: "billing",
				to: "/dashboard?panel=settings&tab=billing",
			},
			{
				label: "Notifications",
				icon: "notifications",
				to: "/dashboard?panel=settings&tab=notifications",
			},
			{
				label: "Security",
				icon: "security",
				to: "/dashboard?panel=settings&tab=security",
			},
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
