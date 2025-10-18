// Base interface with common properties for all menu items
export interface MenuBase {
	translationKey: string;
	ariaLabelKey?: string;
	icon?: string;
	condition?: boolean;
}

// Simple menu item (link)
export interface LinkMenuItem extends MenuBase {
	type: "link";
	href: string;
	target?: string;
}

// Menu item with children (dropdown)
export interface DropdownMenuItem extends MenuBase {
	type: "dropdown";
	children: MenuItem[];
}

// Union type - a menu item can be either a simple link or a dropdown
export type MenuItem = LinkMenuItem | DropdownMenuItem;

// For backward compatibility
export interface DropdownMenu {
	items: MenuItem[];
	icon?: string;
	translationKey?: string;
}
