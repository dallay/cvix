import { createRouter, createWebHistory } from "vue-router";
import { authRoutes } from "@/core/authentication/presentation/authRoutes";
import { useAuthStore } from "@/core/authentication/presentation/stores/authStore";
import { workspaceGuard } from "@/core/workspace/infrastructure/router/workspaceGuard";

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		...authRoutes,
		{
			path: "/",
			redirect: "/dashboard",
		},
		{
			path: "/resume",
			name: "ResumeGenerator",
			component: () =>
				import(
					"@/core/resume/infrastructure/presentation/pages/ResumeGeneratorPage.vue"
				),
			meta: {
				requiresAuth: true,
			},
		},
		{
			path: "/settings",
			name: "Settings",
			component: () => import("@/pages/SettingsPage.vue"),
			meta: {
				requiresAuth: true,
			},
		},
	],
});

// Navigation guard for authentication
router.beforeEach(async (to, from, next) => {
	const authStore = useAuthStore();

	// Check authentication status on first load or when navigating to a protected route
	// Skip checkAuth if we're coming from login page (we just authenticated)
	const comingFromLogin = from.path === "/login";
	if (!authStore.isAuthenticated && to.meta.requiresAuth && !comingFromLogin) {
		try {
			await authStore.checkAuth();
		} catch {
			// User is not authenticated
		}
	}

	// Handle routes that require authentication
	if (to.meta.requiresAuth && !authStore.isAuthenticated) {
		next({
			path: "/login",
			query: { redirect: to.fullPath },
		});
		return;
	}

	// Handle routes that require guest (not authenticated)
	if (to.meta.requiresGuest && authStore.isAuthenticated) {
		const redirectPath = (to.query.redirect as string) || "/dashboard";
		next(redirectPath);
		return;
	}

	// Handle routes that require specific roles
	if (to.meta.requiresAuth && to.meta.roles) {
		const requiredRoles = Array.isArray(to.meta.roles)
			? to.meta.roles
			: [to.meta.roles];
		const hasRequiredRole = requiredRoles.some((role) =>
			authStore.hasRole(role as string),
		);

		if (!hasRequiredRole) {
			// User doesn't have required role, redirect to unauthorized page
			next({
				path: "/unauthorized",
				query: { from: to.fullPath },
			});
			return;
		}
	}

	next();
});

// Navigation guard for workspace loading
// Must run AFTER authentication guard to ensure user is authenticated
router.beforeEach(workspaceGuard);

export default router;
