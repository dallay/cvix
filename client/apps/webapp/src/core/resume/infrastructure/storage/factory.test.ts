import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
	createResumeStorage,
	getDefaultStorageType,
	getStorageMetadata,
	getStorageMetadataByType,
} from "./factory";
import { IndexedDBResumeStorage } from "./IndexedDBResumeStorage";
import { LocalStorageResumeStorage } from "./LocalStorageResumeStorage";
import { RemoteResumeStorage } from "./RemoteResumeStorage";
import { SessionStorageResumeStorage } from "./SessionStorageResumeStorage";

describe("Storage Factory", () => {
	describe("createResumeStorage", () => {
		it("should create session storage instance", () => {
			const storage = createResumeStorage("session");
			expect(storage).toBeInstanceOf(SessionStorageResumeStorage);
			expect(storage.type()).toBe("session");
		});

		it("should create local storage instance", () => {
			const storage = createResumeStorage("local");
			expect(storage).toBeInstanceOf(LocalStorageResumeStorage);
			expect(storage.type()).toBe("local");
		});

		it("should create indexeddb storage instance", () => {
			const storage = createResumeStorage("indexeddb");
			expect(storage).toBeInstanceOf(IndexedDBResumeStorage);
			expect(storage.type()).toBe("indexeddb");
		});

		it("should create remote storage instance", () => {
			const storage = createResumeStorage("remote");
			expect(storage).toBeInstanceOf(RemoteResumeStorage);
			expect(storage.type()).toBe("remote");
		});

		it("should throw error for unknown storage type", () => {
			expect(() => createResumeStorage("unknown" as never)).toThrow(
				"Unknown storage type",
			);
		});
	});

	describe("getDefaultStorageType", () => {
		let originalEnv: boolean;

		beforeEach(() => {
			originalEnv = import.meta.env.DEV;
		});

		afterEach(() => {
			import.meta.env.DEV = originalEnv;
		});

		it("should return session in development mode", () => {
			import.meta.env.DEV = true;
			const type = getDefaultStorageType();
			expect(type).toBe("session");
		});

		it("should return local in production mode", () => {
			import.meta.env.DEV = false;
			const type = getDefaultStorageType();
			expect(type).toBe("local");
		});

		// Note: Testing localStorage unavailability fallback is difficult without
		// mocking the entire localStorage object. This behavior is verified manually
		// in environments where localStorage is disabled.
	});

	describe("getStorageMetadata", () => {
		it("should return metadata for all storage types", () => {
			const metadata = getStorageMetadata();

			expect(metadata).toHaveLength(4);
			expect(metadata.map((m) => m.type)).toEqual([
				"session",
				"local",
				"indexeddb",
				"remote",
			]);
		});

		it("should include required fields for each storage type", () => {
			const metadata = getStorageMetadata();

			metadata.forEach((item) => {
				expect(item).toHaveProperty("type");
				expect(item).toHaveProperty("label");
				expect(item).toHaveProperty("description");
				expect(item).toHaveProperty("icon");
				expect(item).toHaveProperty("persistence");
				expect(item).toHaveProperty("capacity");
			});
		});

		it("should mark local storage as recommended", () => {
			const metadata = getStorageMetadata();
			const localMeta = metadata.find((m) => m.type === "local");

			expect(localMeta?.recommended).toBe(true);
		});

		it("should have correct persistence types", () => {
			const metadata = getStorageMetadata();

			const sessionMeta = metadata.find((m) => m.type === "session");
			expect(sessionMeta?.persistence).toBe("session");

			const localMeta = metadata.find((m) => m.type === "local");
			expect(localMeta?.persistence).toBe("permanent");

			const indexedDBMeta = metadata.find((m) => m.type === "indexeddb");
			expect(indexedDBMeta?.persistence).toBe("permanent");
		});

		it("should have capacity information for all types", () => {
			const metadata = getStorageMetadata();

			metadata.forEach((item) => {
				expect(item.capacity).toBeTruthy();
				expect(typeof item.capacity).toBe("string");
			});
		});
	});

	describe("getStorageMetadataByType", () => {
		it("should return metadata for session storage", () => {
			const metadata = getStorageMetadataByType("session");

			expect(metadata).toBeDefined();
			expect(metadata?.type).toBe("session");
			expect(metadata?.label).toBe("Session Storage");
		});

		it("should return metadata for local storage", () => {
			const metadata = getStorageMetadataByType("local");

			expect(metadata).toBeDefined();
			expect(metadata?.type).toBe("local");
			expect(metadata?.label).toBe("Local Storage");
			expect(metadata?.recommended).toBe(true);
		});

		it("should return metadata for indexeddb storage", () => {
			const metadata = getStorageMetadataByType("indexeddb");

			expect(metadata).toBeDefined();
			expect(metadata?.type).toBe("indexeddb");
			expect(metadata?.label).toBe("IndexedDB");
		});

		it("should return undefined for unknown storage type", () => {
			const metadata = getStorageMetadataByType("unknown" as never);
			expect(metadata).toBeUndefined();
		});

		it("should return metadata for remote storage type", () => {
			const metadata = getStorageMetadataByType("remote");

			expect(metadata).toBeDefined();
			expect(metadata?.type).toBe("remote");
			expect(metadata?.label).toBe("Cloud Storage");
			expect(metadata?.recommended).toBe(true);
		});
	});
});
