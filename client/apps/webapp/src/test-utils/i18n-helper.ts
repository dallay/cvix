import { createI18n } from "vue-i18n";
import enMessages from "./locales/en-test.json";
import esMessages from "./locales/es-test.json";

export function createTestI18n() {
	return createI18n({
		legacy: false,
		locale: "en",
		messages: {
			en: enMessages,
			es: esMessages,
		},
	});
}
