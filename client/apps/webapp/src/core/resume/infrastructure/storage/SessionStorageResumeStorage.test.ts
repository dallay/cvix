import { afterEach, beforeEach, describe, expect, it } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { PartialResume } from "@/core/resume/domain/ResumeStorage";
import { SessionStorageResumeStorage } from "./SessionStorageResumeStorage";

// Helper factory to create a Storage implementation that throws for a specified method
const createFailingStorage = (
	failingMethod: "setItem" | "getItem" | "removeItem",
	errorMessage = "Storage error",
): Storage => ({
	length: 0,
	clear: () => {},
	key: (_index: number) => null,
	getItem: (_key: string) => {
		if (failingMethod === "getItem") throw new Error(errorMessage);
		return null;
	},
	setItem: (_key: string, _value: string) => {
		if (failingMethod === "setItem") throw new Error(errorMessage);
	},
	removeItem: (_key: string) => {
		if (failingMethod === "removeItem") throw new Error(errorMessage);
	},
});

describe("SessionStorageResumeStorage", () => {
	let storage: SessionStorageResumeStorage;
	let mockResume: Resume;

	beforeEach(() => {
		storage = new SessionStorageResumeStorage();
		sessionStorage.clear();

		mockResume = {
			basics: {
				name: "John Doe",
				label: "Software Engineer",
				image: "https://example.com/photo.jpg",
				email: "john@example.com",
				phone: "+1-555-0100",
				url: "https://johndoe.com",
				summary: "Experienced software engineer",
				location: {
					address: "123 Main St",
					postalCode: "12345",
					city: "San Francisco",
					countryCode: "US",
					region: "California",
				},
				profiles: [],
			},
			work: [],
			volunteer: [],
			education: [],
			awards: [],
			certificates: [],
			publications: [],
			skills: [],
			languages: [],
			interests: [],
			references: [],
			projects: [],
		};
	});

	afterEach(() => {
		sessionStorage.clear();
	});

	describe("save", () => {
		it("should save resume to session storage", async () => {
			const result = await storage.save(mockResume);

			expect(result.data).toEqual(mockResume);
			expect(result.storageType).toBe("session");
			expect(result.timestamp).toBeDefined();

			const saved = sessionStorage.getItem("cvix:resume");
			expect(saved).not.toBeNull();
			expect(JSON.parse(saved as string)).toEqual(mockResume);
		});

		it("should save partial resume to session storage", async () => {
			const partialResume: PartialResume = {
				basics: {
					name: "Jane Doe",
					email: "jane@example.com",
				},
			};

			const result = await storage.save(partialResume);

			expect(result.data).toEqual(partialResume);
			expect(result.storageType).toBe("session");

			const saved = sessionStorage.getItem("cvix:resume");
			expect(saved).not.toBeNull();
			expect(JSON.parse(saved as string)).toEqual(partialResume);
		});

		it("should overwrite existing data", async () => {
			await storage.save(mockResume);

			const updatedResume = {
				...mockResume,
				basics: {
					...mockResume.basics,
					name: "Jane Smith",
				},
			};

			await storage.save(updatedResume);

			const saved = sessionStorage.getItem("cvix:resume");
			expect(saved).not.toBeNull();
			const parsed = JSON.parse(saved as string);
			expect(parsed.basics.name).toBe("Jane Smith");
		});

		it("should handle storage errors gracefully", async () => {
			const failing = new SessionStorageResumeStorage(
				createFailingStorage("setItem", "QuotaExceededError"),
			);
			await expect(failing.save(mockResume)).rejects.toThrow(
				/Failed to save resume to session storage/,
			);
		});
	});

	describe("load", () => {
		it("should load resume from session storage", async () => {
			sessionStorage.setItem("cvix:resume", JSON.stringify(mockResume));

			const result = await storage.load();

			expect(result.data).toEqual(mockResume);
			expect(result.storageType).toBe("session");
			expect(result.timestamp).toBeDefined();
		});

		it("should return null when no data exists", async () => {
			const result = await storage.load();

			expect(result.data).toBeNull();
			expect(result.storageType).toBe("session");
		});

		it("should handle invalid JSON gracefully", async () => {
			sessionStorage.setItem("cvix:resume", "invalid json{]");

			await expect(storage.load()).rejects.toThrow(
				/Failed to load resume from session storage/,
			);
		});

		it("should handle storage errors gracefully", async () => {
			const failing = new SessionStorageResumeStorage(
				createFailingStorage("getItem", "Storage error"),
			);
			await expect(failing.load()).rejects.toThrow(
				/Failed to load resume from session storage/,
			);
		});
	});

	describe("clear", () => {
		it("should clear resume from session storage", async () => {
			sessionStorage.setItem("cvix:resume", JSON.stringify(mockResume));

			await storage.clear();

			const saved = sessionStorage.getItem("cvix:resume");
			expect(saved).toBeNull();
		});

		it("should not throw when clearing empty storage", async () => {
			await expect(storage.clear()).resolves.not.toThrow();
		});

		it("should handle storage errors gracefully", async () => {
			const failing = new SessionStorageResumeStorage(
				createFailingStorage("removeItem", "Storage error"),
			);
			await expect(failing.clear()).rejects.toThrow(
				/Failed to clear resume from session storage/,
			);
		});
	});

	describe("type", () => {
		it("should return session as storage type", () => {
			expect(storage.type()).toBe("session");
		});
	});

	describe("integration", () => {
		it("should handle save-load-clear workflow", async () => {
			// Save
			await storage.save(mockResume);

			// Load
			const loadResult = await storage.load();
			expect(loadResult.data).toEqual(mockResume);

			// Clear
			await storage.clear();

			// Load again (should be empty)
			const emptyResult = await storage.load();
			expect(emptyResult.data).toBeNull();
		});
	});
});
