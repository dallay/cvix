// Vitest setup file for logger package
// This file is intentionally minimal as the logger package doesn't need complex setup

import { afterEach, beforeEach } from "vitest";

// Helper to clean up global logger state
function cleanupGlobalLoggerState() {
	if (typeof globalThis !== "undefined") {
		// biome-ignore lint/suspicious/noExplicitAny: GlobalThis is used for browser compatibility
		delete (globalThis as any).__LOGGER_MANAGER__;
	}
}

beforeEach(() => {
	cleanupGlobalLoggerState();
});

afterEach(() => {
	cleanupGlobalLoggerState();
});
