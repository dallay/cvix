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

const createMockStorage = (): Storage => {
	const store = new Map<string, string>();
	return {
		getItem: vi.fn((key: string) => store.get(key) ?? null),
		setItem: vi.fn((key: string, value: string) => {
			store.set(key, value);
		}),
		removeItem: vi.fn((key: string) => {
			store.delete(key);
		}),
		clear: vi.fn(() => {
			store.clear();
		}),
		key: vi.fn((index: number) => {
			const keys = Array.from(store.keys());
			return keys[index] ?? null;
		}),
		get length() {
			return store.size;
		},
	} as Storage;
};

global.sessionStorage = createMockStorage();
global.localStorage = createMockStorage();

// Refined mock for window object
global.window = Object.create(global);
Object.assign(global.window, {
	sessionStorage: global.sessionStorage,
	localStorage: global.localStorage,
	addEventListener: vi.fn(),
	removeEventListener: vi.fn(),
	navigator: { userAgent: "node.js" },
});
