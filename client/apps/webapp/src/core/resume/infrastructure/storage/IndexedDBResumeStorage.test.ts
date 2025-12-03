import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { PartialResume } from "@/core/resume/domain/ResumeStorage";
import { IndexedDBResumeStorage } from "./IndexedDBResumeStorage";

// Mock IndexedDB
const createMockIDBStore = () => {
	const store = new Map<string, unknown>();
	return {
		put: vi.fn((data: unknown, key: string) => {
			store.set(key, data);
			return { onsuccess: null, onerror: null } as IDBRequest;
		}),
		get: vi.fn((key: string) => {
			const result = store.get(key);
			return {
				result,
				onsuccess: null,
				onerror: null,
			} as unknown as IDBRequest;
		}),
		delete: vi.fn((key: string) => {
			store.delete(key);
			return { onsuccess: null, onerror: null } as IDBRequest;
		}),
		_store: store,
	};
};

const createMockIDBTransaction = (
	store: ReturnType<typeof createMockIDBStore>,
) => ({
	objectStore: vi.fn(() => store),
});

const createMockIDBDatabase = (
	transaction: ReturnType<typeof createMockIDBTransaction>,
) => ({
	transaction: vi.fn(() => transaction),
	close: vi.fn(),
	objectStoreNames: {
		contains: vi.fn(() => true),
	},
	createObjectStore: vi.fn(),
});

describe("IndexedDBResumeStorage", () => {
	let storage: IndexedDBResumeStorage;
	let mockStore: ReturnType<typeof createMockIDBStore>;
	let mockTransaction: ReturnType<typeof createMockIDBTransaction>;
	let mockDatabase: ReturnType<typeof createMockIDBDatabase>;
	let mockResume: Resume;

	beforeEach(() => {
		mockStore = createMockIDBStore();
		mockTransaction = createMockIDBTransaction(mockStore);
		mockDatabase = createMockIDBDatabase(mockTransaction);

		// Mock indexedDB.open
		const mockOpenRequest = {
			result: mockDatabase,
			error: null,
			onsuccess: null as ((event: Event) => void) | null,
			onerror: null as ((event: Event) => void) | null,
			onupgradeneeded: null as ((event: IDBVersionChangeEvent) => void) | null,
		};

		vi.stubGlobal("indexedDB", {
			open: vi.fn(() => {
				// Simulate async success
				setTimeout(() => {
					if (mockOpenRequest.onsuccess) {
						mockOpenRequest.onsuccess({
							target: mockOpenRequest,
						} as unknown as Event);
					}
				}, 0);
				return mockOpenRequest;
			}),
		});

		storage = new IndexedDBResumeStorage();

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
		vi.unstubAllGlobals();
	});

	describe("type", () => {
		it("should return indexeddb as storage type", () => {
			expect(storage.type()).toBe("indexeddb");
		});
	});

	describe("save", () => {
		it("should save resume to IndexedDB", async () => {
			// Setup mock to succeed
			mockStore.put.mockImplementation((_data: unknown, _key: string) => {
				const request = {
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
				};
				setTimeout(() => {
					if (request.onsuccess) {
						request.onsuccess({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			const result = await storage.save(mockResume);

			expect(result.data).toEqual(mockResume);
			expect(result.storageType).toBe("indexeddb");
			expect(result.timestamp).toBeDefined();
		});

		it("should save partial resume to IndexedDB", async () => {
			const partialResume: PartialResume = {
				basics: {
					name: "Jane Doe",
					email: "jane@example.com",
				},
			};

			mockStore.put.mockImplementation((_data: unknown, _key: string) => {
				const request = {
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
				};
				setTimeout(() => {
					if (request.onsuccess) {
						request.onsuccess({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			const result = await storage.save(partialResume);

			expect(result.data).toEqual(partialResume);
			expect(result.storageType).toBe("indexeddb");
		});

		it("should handle save errors gracefully", async () => {
			mockStore.put.mockImplementation(() => {
				const request = {
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
					error: { message: "QuotaExceededError" },
				};
				setTimeout(() => {
					if (request.onerror) {
						request.onerror({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			await expect(storage.save(mockResume)).rejects.toThrow(
				/Failed to save resume to IndexedDB/,
			);
		});
	});

	describe("load", () => {
		it("should load resume from IndexedDB", async () => {
			const storedData = {
				resume: mockResume,
				timestamp: new Date().toISOString(),
			};

			mockStore.get.mockImplementation(() => {
				const request = {
					result: storedData,
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
				};
				setTimeout(() => {
					if (request.onsuccess) {
						request.onsuccess({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			const result = await storage.load();

			expect(result.data).toEqual(mockResume);
			expect(result.storageType).toBe("indexeddb");
			expect(result.metadata?.savedAt).toBeDefined();
		});

		it("should return null when no data exists", async () => {
			mockStore.get.mockImplementation(() => {
				const request = {
					result: undefined,
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
				};
				setTimeout(() => {
					if (request.onsuccess) {
						request.onsuccess({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			const result = await storage.load();

			expect(result.data).toBeNull();
			expect(result.storageType).toBe("indexeddb");
		});

		it("should handle load errors gracefully", async () => {
			mockStore.get.mockImplementation(() => {
				const request = {
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
					error: { message: "Storage error" },
				};
				setTimeout(() => {
					if (request.onerror) {
						request.onerror({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			await expect(storage.load()).rejects.toThrow(
				/Failed to load resume from IndexedDB/,
			);
		});
	});

	describe("clear", () => {
		it("should clear resume from IndexedDB", async () => {
			mockStore.delete.mockImplementation(() => {
				const request = {
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
				};
				setTimeout(() => {
					if (request.onsuccess) {
						request.onsuccess({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			await expect(storage.clear()).resolves.not.toThrow();
		});

		it("should handle clear errors gracefully", async () => {
			mockStore.delete.mockImplementation(() => {
				const request = {
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
					error: { message: "Storage error" },
				};
				setTimeout(() => {
					if (request.onerror) {
						request.onerror({} as Event);
					}
				}, 0);
				return request as unknown as IDBRequest;
			});

			await expect(storage.clear()).rejects.toThrow(
				/Failed to clear resume from IndexedDB/,
			);
		});
	});

	describe("database opening", () => {
		it("should handle database open errors", async () => {
			vi.stubGlobal("indexedDB", {
				open: vi.fn(() => {
					const request = {
						result: null,
						error: { message: "Database error" },
						onsuccess: null as ((event: Event) => void) | null,
						onerror: null as ((event: Event) => void) | null,
					};
					setTimeout(() => {
						if (request.onerror) {
							request.onerror({} as Event);
						}
					}, 0);
					return request;
				}),
			});

			const failingStorage = new IndexedDBResumeStorage();

			await expect(failingStorage.load()).rejects.toThrow(/Failed to/);
		});

		it("should create object store on upgrade if not exists", async () => {
			const mockDb = {
				...mockDatabase,
				objectStoreNames: {
					contains: vi.fn(() => false),
				},
				createObjectStore: vi.fn(),
			};

			vi.stubGlobal("indexedDB", {
				open: vi.fn(() => {
					const request = {
						result: mockDb,
						error: null,
						onsuccess: null as ((event: Event) => void) | null,
						onerror: null as ((event: Event) => void) | null,
						onupgradeneeded: null as
							| ((event: IDBVersionChangeEvent) => void)
							| null,
					};
					setTimeout(() => {
						// First trigger upgrade
						if (request.onupgradeneeded) {
							request.onupgradeneeded({
								target: { result: mockDb },
							} as unknown as IDBVersionChangeEvent);
						}
						// Then success
						if (request.onsuccess) {
							request.onsuccess({ target: request } as unknown as Event);
						}
					}, 0);
					return request;
				}),
			});

			const upgradeStorage = new IndexedDBResumeStorage();

			mockStore.get.mockImplementation(() => {
				const req = {
					result: undefined,
					onsuccess: null as ((event: Event) => void) | null,
					onerror: null as ((event: Event) => void) | null,
				};
				setTimeout(() => {
					if (req.onsuccess) {
						req.onsuccess({} as Event);
					}
				}, 0);
				return req as unknown as IDBRequest;
			});

			await upgradeStorage.load();

			expect(mockDb.createObjectStore).toHaveBeenCalledWith("resumes");
		});
	});
});
