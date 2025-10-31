// Declarations for unplugin-icons virtual modules
// Allows imports like: import PhGlobeLight from '~icons/ph/globe-light';
declare module "~icons/ph/*" {
	import type { DefineComponent } from "vue";
	const component: DefineComponent<Record<string, unknown>, unknown, unknown>;
	export default component;
}

declare module "~icons/*" {
	import type { DefineComponent } from "vue";
	const component: DefineComponent<Record<string, unknown>, unknown, unknown>;
	export default component;
}

// Some setups reference the virtual:icons path directly
declare module "virtual:icons/*" {
	import type { DefineComponent } from "vue";
	const component: DefineComponent<Record<string, unknown>, unknown, unknown>;
	export default component;
}
