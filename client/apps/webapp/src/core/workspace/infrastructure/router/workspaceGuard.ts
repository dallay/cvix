import type { NavigationGuardNext, RouteLocationNormalized } from "vue-router";
import { useAuthStore } from "@/core/authentication/presentation/stores/authStore.ts";
import { useWorkspaceLoader } from "../../application/useWorkspaceLoader.ts";
import { useWorkspaceStore } from "../store/workspaceStore.ts";

const isDev = import.meta.env.DEV;

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

	const workspaceStore = useWorkspaceStore();

	// Skip if already loaded in this session
	if (workspaceStore.loadedInSession) {
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
		const { loadWorkspaceOnLogin } = useWorkspaceLoader();
		await loadWorkspaceOnLogin(authStore.user.id);

		workspaceStore.loadedInSession = true;

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
	const workspaceStore = useWorkspaceStore();
	workspaceStore.resetSession();
	if (isDev) {
		console.log("[Workspace Guard] Session reset");
	}
}
