import { createPinia } from "pinia";
import { createApp } from "vue";

import App from "./App.vue";
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

	app.mount("#app");
});
