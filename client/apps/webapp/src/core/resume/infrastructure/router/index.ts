import type { RouteRecordRaw } from "vue-router";

export const resumeRoutes: RouteRecordRaw[] = [
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
];
