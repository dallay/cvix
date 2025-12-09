import {defineConfig} from "@playwright/test";

// Choose a safe backend target for local Playwright runs to avoid Vite proxying to
// an unavailable HTTPS dev backend (e.g., https://localhost:8443). Priority:
// 1. PLAYWRIGHT_BACKEND_URL (explicit override for tests)
// 2. If running on CI: use BACKEND_URL (if provided) or fallback to http://localhost:8080
// 3. Local dev: default to http://localhost:8080
const defaultBackendForTests =
	process.env.PLAYWRIGHT_BACKEND_URL ??
    	process.env.BACKEND_URL ??
    	"http://localhost:8080";
export default defineConfig({
	webServer: {
		command: "pnpm run dev:web",
		port: 9876,
		timeout: 120 * 1000, // 2 minutes
		reuseExistingServer: !process.env.CI,
		// Provide an explicit BACKEND_URL to the dev server process so Vite's proxy
		// targets a reachable backend during tests. This avoids ECONNREFUSED errors
		// when the developer doesn't have the HTTPS backend running on 8443.
		env: {
			BACKEND_URL: defaultBackendForTests,
		},
	},
	testDir: "./e2e",
});
