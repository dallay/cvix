import type { InjectionKey } from "vue";
import type { ResumeGenerator } from "@/core/resume/domain/ResumeGenerator.ts";
import type { ResumeStorage } from "@/core/resume/domain/ResumeStorage.ts";
import type { ResumeValidator } from "@/core/resume/domain/ResumeValidator.ts";

/**
 * Injection key for the Resume Validator.
 * Used to provide/inject the validator instance across the application.
 *
 * @example
 * // In main.ts or app setup
 * app.provide(RESUME_VALIDATOR_KEY, new JsonResumeValidator());
 *
 * @example
 * // In a component or composable
 * const validator = inject(RESUME_VALIDATOR_KEY);
 */
export const RESUME_VALIDATOR_KEY: InjectionKey<ResumeValidator> =
	Symbol("ResumeValidator");

/**
 * Injection key for the Resume Generator.
 * Used to provide/inject the generator instance across the application.
 *
 * @example
 * // In main.ts or app setup
 * app.provide(RESUME_GENERATOR_KEY, new ResumeHttpClient());
 *
 * @example
 * // In a component or composable
 * const generator = inject(RESUME_GENERATOR_KEY);
 */
export const RESUME_GENERATOR_KEY: InjectionKey<ResumeGenerator> =
	Symbol("ResumeGenerator");

/**
 * Injection key for the Resume Storage.
 * Used to provide/inject the storage instance across the application.
 *
 * @example
 * // In main.ts or app setup
 * app.provide(RESUME_STORAGE_KEY, new SessionStorageResumeStorage());
 *
 * @example
 * // In a component or composable
 * const storage = inject(RESUME_STORAGE_KEY);
 */
export const RESUME_STORAGE_KEY: InjectionKey<ResumeStorage> =
	Symbol("ResumeStorage");
