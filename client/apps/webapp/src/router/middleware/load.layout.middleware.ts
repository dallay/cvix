import type { RouteLocationNormalized } from "vue-router";

/**
 * This middleware is used to dynamically update the Layouts system.
 *
 * As soon as the route changes, it tries to pull the layout that we want to display from meta.
 * Then it loads the layout component, and assigns the loaded component to the meta layoutComponent variable.
 * And layoutComponent is used in the main layout AppLayout.vue for dynamic component switching.
 *
 * If the layout we want to display is not found, loads the default layout DashboardLayout.
 */
export async function loadLayoutMiddleware(route: RouteLocationNormalized) {
	try {
		// Get layout name from route meta, default to 'DashboardLayout' if not specified
		const layout = route.meta.layout || "DashboardLayout";

		// Dynamically import the layout component
		// Validate layout name to prevent path traversal
		const validLayoutName = /^[a-zA-Z0-9_-]+$/.test(String(layout));
		if (!validLayoutName) {
			throw new Error(`Invalid layout name: ${layout}`);
		}

		// Dynamically import the layout component
		const layoutComponent = await import(`@/layouts/${layout}.vue`);

		// Store the loaded component in route meta for use in the main layout
		route.meta.layoutComponent = layoutComponent.default;
	} catch (error) {
		console.error("Failed to load layout component:", error);

		// Load default layout on error
		try {
			const defaultLayoutComponent = await import("@/layouts/AppLayout.vue");
			route.meta.layoutComponent = defaultLayoutComponent.default;
		} catch (defaultError) {
			console.error("Failed to load default layout:", defaultError);
		}
	}
}
