import "vue-router";

declare module "vue-router" {
	interface RouteMeta {
		/**
		 * Indicates if the route requires authentication
		 */
		requiresAuth?: boolean;

		/**
		 * Indicates if the route is only for guests (unauthenticated users)
		 */
		requiresGuest?: boolean;

		/**
		 * Array of roles required to access the route
		 * User must have at least one of these roles
		 */
		roles?: string | string[];

		/**
		 * Custom page title
		 */
		title?: string;
	}
}
