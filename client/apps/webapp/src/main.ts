import { createPinia } from "pinia";
import { createApp } from "vue";

import App from "./App.vue";
import { RESUME_STORAGE_KEY } from "./core/resume/infrastructure/di";
import { createResumeStorage } from "./core/resume/infrastructure/storage";
import {
	LocalStorageSettingsRepository,
	SETTINGS_REPOSITORY_KEY,
} from "./core/settings";
import { i18n } from "./i18n";
import router from "./router";
import { csrfService } from "./shared/csrf.service";
import "./styles/globals.css";
import type { StorageType } from "./core/resume/domain/ResumeStorage";

/**
 * Gets the user's storage preference from localStorage.
 * Falls back to 'session' if no preference is set.
 */
function getUserStoragePreference(): StorageType {
	try {
		const saved = localStorage.getItem("cvix:user-settings");
		if (saved) {
			const settings = JSON.parse(saved);
			const preference = settings.storagePreference;
			if (
				preference &&
				["session", "local", "indexeddb", "remote"].includes(preference)
			) {
				return preference as StorageType;
			}
		}
	} catch {
		// localStorage might not be available or parsing failed
	}
	return "session"; // Default
}

// Initialize CSRF token before mounting the app
csrfService.initialize().then(() => {
	const app = createApp(App);

	app.use(createPinia());
	app.use(router);
	app.use(i18n);

	// Provide settings repository
	const settingsRepository = new LocalStorageSettingsRepository();
	app.provide(SETTINGS_REPOSITORY_KEY, settingsRepository);

	// Configure resume storage based on user preference
	const storagePreference = getUserStoragePreference();
	const resumeStorage = createResumeStorage(storagePreference);
	app.provide(RESUME_STORAGE_KEY, resumeStorage);

	app.mount("#app");
});
