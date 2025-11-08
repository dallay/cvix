import type { App } from "vue";
import { RESUME_VALIDATOR_KEY } from "@/core/resume/infrastructure/di";
import { JsonResumeValidator } from "@/core/resume/infrastructure/validation";

/**
 * Configures dependency injection for the resume module.
 *
 * This function registers all necessary dependencies for the resume feature,
 * making them available throughout the application via Vue's provide/inject.
 *
 * @param app - The Vue application instance
 *
 * @example
 * // In main.ts
 * import { setupResumeDI } from '@/core/resume/infrastructure/config/di';
 * setupResumeDI(app);
 */
export function setupResumeDI(app: App): void {
	// Register the JSON Resume Validator
	app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator());
}
