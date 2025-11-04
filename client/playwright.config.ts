import { defineConfig } from "@playwright/test";

export default defineConfig({
	webServer: {
		command: "pnpm run dev:web",
		port: 9876,
		timeout: 120 * 1000, // 2 minutes
		reuseExistingServer: !process.env.CI,
	},
	testDir: "./e2e",
});
