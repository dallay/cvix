import type { NavigationGuardNext, RouteLocationNormalized } from "vue-router";
import { useAuthStore } from "@/authentication/presentation/stores/authStore";
import { useWorkspaceLoader } from "../../application/useWorkspaceLoader";

const isDev = import.meta.env.DEV;

/**
 * Flag to track if workspace has been loaded for the current session
 */
let workspaceLoadedForSession = false;

/**
 * Navigation guard that automatically loads workspace on first authenticated navigation
 */
export async function workspaceGuard(
	to: RouteLocationNormalized,
	_from: RouteLocationNormalized,
	next: NavigationGuardNext,
): Promise<void> {
	// Only load workspace for authenticated routes
	if (!to.meta.requiresAuth) {
		next();
		return;
	}

	// Skip if already loaded in this session
	if (workspaceLoadedForSession) {
		next();
		return;
	}

	const authStore = useAuthStore();

	// Skip if not authenticated
	if (!authStore.isAuthenticated || !authStore.user?.id) {
		next();
		return;
	}

	try {
		if (isDev) {
			console.log("[Workspace Guard] Loading workspace for authenticated user");
		}

		const { loadWorkspaceOnLogin } = useWorkspaceLoader();
		await loadWorkspaceOnLogin(authStore.user.id);

		workspaceLoadedForSession = true;

		if (isDev) {
			console.log("[Workspace Guard] Workspace loaded successfully");
		}

		next();
	} catch (error) {
		// Log error but don't block navigation
		if (isDev) {
			console.error("[Workspace Guard] Failed to load workspace:", error);
		}

		// Continue navigation even if workspace loading fails
		next();
	}
}

/**
 * Reset the session flag (for testing or logout)
 */
export function resetWorkspaceGuardSession(): void {
	workspaceLoadedForSession = false;
	if (isDev) {
		console.log("[Workspace Guard] Session reset");
	}
}
