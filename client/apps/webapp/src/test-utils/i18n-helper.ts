import { createI18n } from "vue-i18n";
import enMessages from "./locales/en-test.json" with { type: "json" };
import esMessages from "./locales/es-test.json" with { type: "json" };

/**
 * Create a preconfigured Vue I18n instance for tests with English as the default locale.
 *
 * @returns The i18n instance configured with `locale: "en"` and `messages` for `en` and `es`.
 */
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