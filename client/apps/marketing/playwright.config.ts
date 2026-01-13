import { existsSync } from "node:fs";
import { fileURLToPath, URL } from "node:url";
import { CVIX_API_URL, PORTS } from "@cvix/lib";
import { SSL_CERT_PATH, SSL_KEY_PATH } from "@cvix/lib/ssl";
import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright configuration for Marketing Site E2E testing
 * @see https://playwright.dev/docs/test-configuration
 */

// HAR Mocking Strategy:
// Use HAR files to mock API responses and avoid backend dependencies in E2E tests.
// Set USE_HAR=true to enable HAR replay mode (CI/CD default).
// Set USE_HAR=false or RECORD_HAR=true to record new HAR files (requires backend running).
const useHarMocking =
	process.env.USE_HAR === "true" || process.env.CI === "true";

// Choose a safe backend target for local Playwright runs to avoid Vite proxying to
// an unavailable HTTPS dev backend (e.g., https://localhost:8443). Priority:
// 1. PLAYWRIGHT_BACKEND_URL (explicit override for tests)
// 2. CVIX_API_URL (with HTTP fallback for stability)
const defaultBackendForTests =
	process.env.PLAYWRIGHT_BACKEND_URL ??
	(() => {
		try {
			const url = new URL(CVIX_API_URL);
			url.protocol = "http:";
			if (url.port === "8443") {
				url.port = "8080";
			}
			return url.toString();
		} catch {
			// Fallback to original value if URL parsing fails
			return CVIX_API_URL;
		}
	})();

// Detect if SSL certificates exist (via FORCE_HTTP env var or presence of cert files)
// In CI or when FORCE_HTTP is set, use HTTP. In local dev, default to HTTP unless certs exist.
const hasSSLCertificates = (): boolean => {
	const certPath = fileURLToPath(new URL(SSL_CERT_PATH, import.meta.url));
	const keyPath = fileURLToPath(new URL(SSL_KEY_PATH, import.meta.url));
	return existsSync(certPath) && existsSync(keyPath);
};

const hasSSLCerts = hasSSLCertificates();
const useHttp =
	process.env.CI === "true" ||
	process.env.FORCE_HTTP === "true" ||
	!hasSSLCerts;
const baseURL = useHttp
	? `http://localhost:${PORTS.MARKETING}`
	: `https://localhost:${PORTS.MARKETING}`;

export default defineConfig({
	testDir: "./e2e",

	// Test execution settings
	fullyParallel: true,

	// Retry on CI for flake resistance
	retries: process.env.CI ? 2 : 1,

	// Fail if more than 10% of tests are slow
	reportSlowTests: { max: 5, threshold: 60_000 },

	// Limit parallel workers on CI to avoid resource exhaustion
	workers: process.env.CI ? 2 : undefined,

	// Reporting configuration
	reporter: process.env.CI
		? [
				["dot"],
				["html", { open: "never", outputFolder: "playwright-report" }],
				["json", { outputFile: "playwright-report/results.json" }],
				["junit", { outputFile: "playwright-report/results.xml" }],
			]
		: [["list"], ["html", { open: "on-failure" }]],

	// Global configuration for all tests
	use: {
		baseURL,
		trace: process.env.CI ? "on-first-retry" : "off",
		screenshot: process.env.CI ? "only-on-failure" : "off",
		video: "off",
		viewport: { width: 1280, height: 720 },
		colorScheme: "light",
		ignoreHTTPSErrors: true, // Required for self-signed certificates in dev
		actionTimeout: 15_000,
		navigationTimeout: 30_000,
		// HAR replay: Disable automation detection for better HAR replay fidelity
		// (Actual HAR routing with fallback is configured in harFixture.ts)
		...(useHarMocking && {
			launchOptions: {
				args: ["--disable-blink-features=AutomationControlled"],
			},
		}),
	},

	// Development server configuration - auto-start on test run
	webServer: {
		command: "pnpm run dev",
		url: baseURL,
		timeout: 300_000, // 5 minutes to account for dependency builds
		reuseExistingServer: !process.env.CI,
		ignoreHTTPSErrors: true, // Required for self-signed certificates
		env: {
			CVIX_API_URL: defaultBackendForTests,
			FORCE_HTTP: useHttp ? "true" : "false",
			PLAYWRIGHT_TEST: "true",
		},
		stdout: "pipe",
		stderr: "pipe",
	},

	// Browser projects - test across major browsers
	projects: [
		{
			name: "chromium",
			use: { ...devices["Desktop Chrome"] },
		},
		{
			name: "firefox",
			use: { ...devices["Desktop Firefox"] },
		},
		{
			name: "webkit",
			use: {
				...devices["Desktop Safari"],
				// Webkit-specific settings for stability
				launchOptions: {
					slowMo: 50, // Slow down operations slightly for webkit
				},
			},
		},
	],
});
