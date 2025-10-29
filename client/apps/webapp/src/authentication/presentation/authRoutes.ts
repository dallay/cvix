import type { RouteRecordRaw } from "vue-router";

export const authRoutes: RouteRecordRaw[] = [
	{
		path: "/login",
		name: "Login",
		component: () =>
			import("@/authentication/presentation/pages/LoginPage.vue"),
		meta: {
			requiresGuest: true,
			title: "Login",
		},
	},
	{
		path: "/register",
		name: "Register",
		component: () =>
			import("@/authentication/presentation/pages/RegisterPage.vue"),
		meta: {
			requiresGuest: true,
			title: "Register",
		},
	},
	{
		path: "/forgot-password",
		name: "ForgotPassword",
		component: () =>
			import("@/authentication/presentation/pages/ForgotPasswordPage.vue"),
		meta: {
			requiresGuest: true,
			title: "Forgot Password",
		},
	},
	{
		path: "/unauthorized",
		name: "Unauthorized",
		component: () =>
			import("@/authentication/presentation/pages/UnauthorizedPage.vue"),
		meta: {
			title: "Unauthorized",
		},
	},
	{
		path: "/dashboard",
		name: "Dashboard",
		component: () =>
			import("@/authentication/presentation/pages/DashboardPage.vue"),
		meta: {
			requiresAuth: true,
			title: "Dashboard",
			// Optional: Add roles to restrict access
			// roles: ["ROLE_USER", "ROLE_ADMIN"],
		},
	},
	// Example of an admin-only route
	// {
	// 	path: "/admin",
	// 	name: "AdminPanel",
	// 	component: () => import("@/admin/presentation/pages/AdminPage.vue"),
	// 	meta: {
	// 		requiresAuth: true,
	// 		roles: ["ROLE_ADMIN"],
	// 		title: "Admin Panel",
	// 	},
	// },
];
