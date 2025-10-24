import type { Directive, DirectiveBinding } from "vue";
import { useAuthStore } from "../stores/authStore";

interface RoleBinding {
	roles: string | string[];
	mode?: "any" | "all"; // "any" = has any role, "all" = has all roles
}

/**
 * Custom directive to show/hide elements based on user roles
 *
 * @example
 * ```vue
 * <!-- Show only if user has ROLE_ADMIN -->
 * <button v-role="'ROLE_ADMIN'">Admin Action</button>
 *
 * <!-- Show if user has any of the roles -->
 * <button v-role="{ roles: ['ROLE_ADMIN', 'ROLE_MODERATOR'], mode: 'any' }">
 *   Moderate Content
 * </button>
 *
 * <!-- Show only if user has all the roles -->
 * <button v-role="{ roles: ['ROLE_ADMIN', 'ROLE_SUPER'], mode: 'all' }">
 *   Super Admin Action
 * </button>
 * ```
 */
export const vRole: Directive = {
	mounted(el: HTMLElement, binding: DirectiveBinding<string | RoleBinding>) {
		const authStore = useAuthStore();

		let hasPermission = false;

		if (typeof binding.value === "string") {
			// Simple string role check
			hasPermission = authStore.hasRole(binding.value);
		} else if (binding.value && typeof binding.value === "object") {
			// Object with roles array and mode
			const { roles, mode = "any" } = binding.value;
			const rolesArray = Array.isArray(roles) ? roles : [roles];

			if (mode === "all") {
				hasPermission = rolesArray.every((role) => authStore.hasRole(role));
			} else {
				hasPermission = rolesArray.some((role) => authStore.hasRole(role));
			}
		}

		if (!hasPermission) {
			// Remove the element from DOM if user doesn't have permission
			el.style.display = "none";
			// Or completely remove it:
			// el.parentNode?.removeChild(el);
		}
	},

	updated(el: HTMLElement, binding: DirectiveBinding<string | RoleBinding>) {
		const authStore = useAuthStore();

		let hasPermission = false;

		if (typeof binding.value === "string") {
			hasPermission = authStore.hasRole(binding.value);
		} else if (binding.value && typeof binding.value === "object") {
			const { roles, mode = "any" } = binding.value;
			const rolesArray = Array.isArray(roles) ? roles : [roles];

			if (mode === "all") {
				hasPermission = rolesArray.every((role) => authStore.hasRole(role));
			} else {
				hasPermission = rolesArray.some((role) => authStore.hasRole(role));
			}
		}

		el.style.display = hasPermission ? "" : "none";
	},
};
