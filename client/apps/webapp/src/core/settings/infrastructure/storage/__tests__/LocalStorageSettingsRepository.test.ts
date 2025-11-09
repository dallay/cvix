import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { DEFAULT_USER_SETTINGS } from "../../../domain";
import { LocalStorageSettingsRepository } from "../LocalStorageSettingsRepository";

describe("LocalStorageSettingsRepository", () => {
	let repository: LocalStorageSettingsRepository;

	beforeEach(() => {
		localStorage.clear();
		repository = new LocalStorageSettingsRepository();
	});

	afterEach(() => {
		localStorage.clear();
	});

	describe("load", () => {
		it("should return null when no settings are stored", async () => {
			const result = await repository.load();

			expect(result.success).toBe(true);
			if (result.success) {
				expect(result.data).toBeNull();
			}
		});

		it("should load stored settings from localStorage", async () => {
			const settings = {
				...DEFAULT_USER_SETTINGS,
				storagePreference: "local" as const,
			};

			await repository.save(settings);
			const result = await repository.load();

			expect(result.success).toBe(true);
			if (result.success && result.data) {
				expect(result.data.storagePreference).toBe("local");
			}
		});

		it("should merge stored settings with defaults", async () => {
			// Store partial settings
			localStorage.setItem(
				"cvix:user-settings",
				JSON.stringify({ storagePreference: "indexeddb" }),
			);

			const result = await repository.load();

			expect(result.success).toBe(true);
			if (result.success && result.data) {
				expect(result.data.storagePreference).toBe("indexeddb");
				expect(result.data.theme).toBe(DEFAULT_USER_SETTINGS.theme);
				expect(result.data.language).toBe(DEFAULT_USER_SETTINGS.language);
			}
		});

		it("should handle corrupted data gracefully", async () => {
			localStorage.setItem("cvix:user-settings", "invalid-json");

			const result = await repository.load();

			expect(result.success).toBe(false);
			if (!result.success) {
				expect(result.error).toBeInstanceOf(Error);
			}
		});
	});

	describe("save", () => {
		it("should save settings to localStorage", async () => {
			const settings = {
				...DEFAULT_USER_SETTINGS,
				storagePreference: "local" as const,
			};

			const result = await repository.save(settings);

			expect(result.success).toBe(true);

			const stored = localStorage.getItem("cvix:user-settings");
			expect(stored).not.toBeNull();

			const parsed = JSON.parse(stored as string);
			expect(parsed.storagePreference).toBe("local");
		});

		it("should overwrite existing settings", async () => {
			const settings1 = {
				...DEFAULT_USER_SETTINGS,
				storagePreference: "session" as const,
			};
			const settings2 = {
				...DEFAULT_USER_SETTINGS,
				storagePreference: "local" as const,
			};

			await repository.save(settings1);
			await repository.save(settings2);

			const result = await repository.load();

			expect(result.success).toBe(true);
			if (result.success && result.data) {
				expect(result.data.storagePreference).toBe("local");
			}
		});
	});

	describe("clear", () => {
		it("should remove settings from localStorage", async () => {
			const settings = {
				...DEFAULT_USER_SETTINGS,
				storagePreference: "local" as const,
			};

			await repository.save(settings);
			expect(localStorage.getItem("cvix:user-settings")).not.toBeNull();

			const result = await repository.clear();

			expect(result.success).toBe(true);
			expect(localStorage.getItem("cvix:user-settings")).toBeNull();
		});
	});

	describe("type", () => {
		it("should return localStorage as the type", () => {
			expect(repository.type()).toBe("localStorage");
		});
	});
});
