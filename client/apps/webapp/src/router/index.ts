import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/authentication/presentation/stores/authStore";

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		{
			path: "/login",
			name: "Login",
			component: () =>
				import("@/authentication/presentation/pages/LoginPage.vue"),
			meta: { requiresGuest: true },
		},
		{
			path: "/register",
			name: "Register",
			component: () =>
				import("@/authentication/presentation/pages/RegisterPage.vue"),
			meta: { requiresGuest: true },
		},
		{
			path: "/dashboard",
			name: "Dashboard",
			component: () =>
				import("@/authentication/presentation/pages/DashboardPage.vue"),
			meta: { requiresAuth: true },
		},
		{
			path: "/",
			redirect: "/dashboard",
		},
	],
});

// Navigation guard for authentication
router.beforeEach(async (to, _from, next) => {
	const authStore = useAuthStore();

	// Check authentication status on first load
	if (!authStore.isAuthenticated && to.meta.requiresAuth) {
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
		next("/dashboard");
		return;
	}

	next();
});

export default router;
