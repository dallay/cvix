import { defineConfig, devices } from "@playwright/test";

// Choose a safe backend target for local Playwright runs to avoid Vite proxying to
// an unavailable HTTPS dev backend (e.g., https://localhost:8443). Priority:
// 1. PLAYWRIGHT_BACKEND_URL (explicit override for tests)
// 2. If running on CI: use BACKEND_URL (if provided) or fallback to http://localhost:8080
// 3. Local dev: default to http://localhost:8080
const _defaultBackendForTests =
	process.env.PLAYWRIGHT_BACKEND_URL ??
	process.env.BACKEND_URL ??
	"http://localhost:8080";

export default defineConfig({
	testDir: "./e2e",

	// Configure browser projects
	projects: [
		{
			name: "chromium",
			use: {
				...devices["Desktop Chrome"],
				baseURL: process.env.BASE_URL || "https://localhost:9876",
				ignoreHTTPSErrors: true, // Required for self-signed certificates
			},
		},
	],

	// NOTE: webServer is temporarily disabled during E2E refactor
	// Start the dev server manually with: pnpm run dev:web
	// webServer: {
	//   command: "cd .. && pnpm run dev:web",
	//   port: 9876,
	//   timeout: 120 * 1000, // 2 minutes
	//   reuseExistingServer: !process.env.CI,
	//   env: {
	//     BACKEND_URL: defaultBackendForTests,
	//   },
	// },
});
