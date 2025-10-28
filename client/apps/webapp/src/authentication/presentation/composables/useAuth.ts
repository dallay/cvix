import { computed } from "vue";
import { useAuthStore } from "../stores/authStore";

/**
 * Composable for authorization checks
 * Provides reactive access to user roles and permission checks
 *
 * @example
 * ```typescript
 * const { hasRole, hasAnyRole, hasAllRoles, isAdmin, isUser } = useAuth();
 *
 * // Check single role
 * if (hasRole("ROLE_ADMIN")) {
 *   // Show admin content
 * }
 *
 * // Check if user has any of the roles
 * if (hasAnyRole(["ROLE_ADMIN", "ROLE_MODERATOR"])) {
 *   // Show moderation content
 * }
 * ```
 */
export function useAuth() {
	const authStore = useAuthStore();

	/**
	 * Check if user has a specific role
	 */
	const hasRole = (role: string) => authStore.hasRole(role);

	/**
	 * Check if user has any of the specified roles
	 */
	const hasAnyRole = (roles: string[]) =>
		roles.some((role) => authStore.hasRole(role));

	/**
	 * Check if user has all of the specified roles
	 */
	const hasAllRoles = (roles: string[]) =>
		roles.every((role) => authStore.hasRole(role));

	/**
	 * Check if user is an admin
	 */
	const isAdmin = computed(() => hasRole("ROLE_ADMIN"));

	/**
	 * Check if user is a regular user
	 */
	const isUser = computed(() => hasRole("ROLE_USER"));

	return {
		user: authStore.user,
		isAuthenticated: authStore.isAuthenticated,
		userRoles: authStore.userRoles,
		hasRole,
		hasAnyRole,
		hasAllRoles,
		isAdmin,
		isUser,
	};
}
