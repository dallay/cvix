import type { InjectionKey } from "vue";
import type { SettingsRepository } from "../../domain";

/**
 * Injection key for the SettingsRepository dependency.
 *
 * This key is used with Vue's provide/inject system to inject the settings repository
 * implementation into components that need it.
 *
 * @example
 * ```typescript
 * // In main.ts (provide)
 * import { SETTINGS_REPOSITORY_KEY } from '@/core/settings/infrastructure/di';
 * import { LocalStorageSettingsRepository } from '@/core/settings/infrastructure/storage';
 *
 * app.provide(SETTINGS_REPOSITORY_KEY, new LocalStorageSettingsRepository());
 *
 * // In a component or composable (inject)
 * import { inject } from 'vue';
 * import { SETTINGS_REPOSITORY_KEY } from '@/core/settings/infrastructure/di';
 *
 * const settingsRepo = inject(SETTINGS_REPOSITORY_KEY);
 * ```
 */
export const SETTINGS_REPOSITORY_KEY: InjectionKey<SettingsRepository> =
	Symbol("SettingsRepository");
