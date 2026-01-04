import { createPinia } from "pinia";
import { createApp } from "vue";

import App from "./App.vue";
import { RESUME_STORAGE_KEY } from "./core/resume/infrastructure/di";
import { createResumeStorage } from "./core/resume/infrastructure/storage";
import {
	getUserStoragePreference,
	LocalStorageSettingsRepository,
	SETTINGS_REPOSITORY_KEY,
} from "./core/settings";
import { i18n } from "./i18n";
import router from "./router";
import { csrfService } from "./shared/csrf.service";
import "./styles/globals.css";

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
