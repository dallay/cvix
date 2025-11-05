import { vi } from "vitest";

// Mock import.meta.env
vi.stubGlobal("import.meta", {
	env: {
		LANG: "en", // Default language for tests
		DEV: true,
		PROD: false,
		SSR: true,
	},
	glob: vi.fn((_pattern, _options) => {
		// Mock implementation for import.meta.glob
		// This is used in ui.ts to load translation files
		return {};
	}),
});

// Mock getRelativeLocaleUrl from astro:i18n
vi.mock("astro:i18n", () => ({
	getRelativeLocaleUrl: vi.fn((lang, path) => `/${lang}${path}`),
}));

// Mock sessionStorage and localStorage for Node.js environment
global.sessionStorage = {
	getItem: vi.fn(),
	setItem: vi.fn(),
	removeItem: vi.fn(),
	clear: vi.fn(),
	key: vi.fn(),
	length: 0,
};

global.localStorage = {
	getItem: vi.fn(),
	setItem: vi.fn(),
	removeItem: vi.fn(),
	clear: vi.fn(),
	key: vi.fn(),
	length: 0,
};

// Refined mock for window object
global.window = Object.create(global);
Object.assign(global.window, {
	sessionStorage: global.sessionStorage,
	localStorage: global.localStorage,
	addEventListener: vi.fn(),
	removeEventListener: vi.fn(),
	navigator: { userAgent: "node.js" },
});
