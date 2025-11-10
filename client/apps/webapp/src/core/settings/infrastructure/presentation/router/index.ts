import type { RouteRecordRaw } from "vue-router";

export const settingRoutes: RouteRecordRaw[] = [
	{
		path: "/settings",
		name: "Settings",
		component: () =>
			import(
				"@/core/settings/infrastructure/presentation/pages/SettingsPage.vue"
			),
		meta: {
			requiresAuth: true,
		},
	},
];
