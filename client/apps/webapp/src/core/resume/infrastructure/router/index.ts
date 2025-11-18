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
	{
		path: "/resume/editor",
		name: "ResumeEditor",
		component: () =>
			import(
				"@/core/resume/infrastructure/presentation/pages/ResumeEditorPage.vue"
			),
		meta: {
			requiresAuth: true,
		},
	},
	{
		path: "/resume/pdf",
		name: "ResumePdf",
		component: () =>
			import(
				"@/core/resume/infrastructure/presentation/pages/ResumePdfPage.vue"
			),
		meta: {
			requiresAuth: true,
		},
	},
];
