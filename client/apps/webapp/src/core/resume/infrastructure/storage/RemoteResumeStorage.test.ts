import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import { getCurrentWorkspaceId } from "@/shared/WorkspaceContext";
import type {
	ResumeDocumentResponse,
	ResumeHttpClient,
} from "../http/ResumeHttpClient";
import {
	RemoteResumeStorage,
	type RemoteStorageConfig,
} from "./RemoteResumeStorage";

// Mock the WorkspaceContext module
vi.mock("@/shared/WorkspaceContext", () => ({
	getCurrentWorkspaceId: vi.fn(() => "9dcb2241-6840-4e77-98a3-ddfa89c7d032"),
}));

// Mock localStorage
const localStorageMock = (() => {
	let store: Record<string, string> = {};
	return {
		getItem: vi.fn((key: string) => store[key] ?? null),
		setItem: vi.fn((key: string, value: string) => {
			store[key] = value;
		}),
		removeItem: vi.fn((key: string) => {
			delete store[key];
		}),
		clear: vi.fn(() => {
			store = {};
		}),
	};
})();

Object.defineProperty(globalThis, "localStorage", {
	value: localStorageMock,
	writable: true,
});

describe("RemoteResumeStorage", () => {
	let storage: RemoteResumeStorage;
	let config: RemoteStorageConfig;
	let mockClient: ResumeHttpClient;

	const mockResume: Resume = {
		basics: {
			name: "John Doe",
			label: "Software Engineer",
			image: "",
			email: "john@example.com",
			phone: "",
			url: "",
			summary: "",
			location: {
				address: "",
				city: "",
				region: "",
				countryCode: "",
				postalCode: "",
			},
			profiles: [],
		},
		work: [],
		volunteer: [],
		education: [],
		awards: [],
		publications: [],
		skills: [],
		interests: [],
		references: [],
		projects: [],
		languages: [],
		certificates: [],
	};

	const mockResponse: ResumeDocumentResponse = {
		id: "test-id",
		userId: "user-id",
		workspaceId: "workspace-id",
		title: "Test Resume",
		content: mockResume,
		createdAt: "2024-01-01T00:00:00Z",
		updatedAt: "2024-01-01T00:00:00Z",
		createdBy: "user",
		updatedBy: "user",
	};

	beforeEach(() => {
		vi.resetAllMocks();
		localStorageMock.clear();

		mockClient = {
			createResume: vi.fn(),
			updateResume: vi.fn(),
			getResume: vi.fn(),
			deleteResume: vi.fn(),
			listResumes: vi.fn().mockResolvedValue([]),
		} as Partial<ResumeHttpClient> as ResumeHttpClient;

		config = {
			initialRetryDelay: 0,
			maxRetryDelay: 0,
			maxRetries: 2,
			resumeId: undefined,
		};

		storage = new RemoteResumeStorage(config, mockClient);
	});

	describe("save", () => {
		it("creates a new resume when update returns 404", async () => {
			config.resumeId = "test-resume-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.updateResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 404 },
			});
			(mockClient.createResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				mockResponse,
			);

			const result = await storage.save(mockResume);

			expect(mockClient.updateResume).toHaveBeenCalled();
			// workspaceId is sent via X-Workspace-Id header, not as a parameter
			expect(mockClient.createResume).toHaveBeenCalledWith(
				"test-resume-id",
				mockResume,
				undefined,
			);

			expect(result.data).toEqual(mockResume);
			expect(result.storageType).toBe("remote");
			expect(storage.getResumeId()).toBe(mockResponse.id);
		});

		it("throws when no workspace is selected", async () => {
			vi.mocked(getCurrentWorkspaceId).mockReturnValueOnce(null);

			config.resumeId = "test-resume-id";
			storage = new RemoteResumeStorage(config, mockClient);

			await expect(storage.save(mockResume)).rejects.toThrow(
				"Remote storage operation failed: No workspace selected",
			);
			expect(mockClient.createResume).not.toHaveBeenCalled();
		});

		it("updates existing resume when ID exists", async () => {
			config.resumeId = "existing-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.updateResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				mockResponse,
			);

			await storage.save(mockResume);

			expect(mockClient.updateResume).toHaveBeenCalledWith(
				"existing-id",
				mockResume,
				undefined,
			);
		});

		it("retries and succeeds after transient failure", async () => {
			(mockClient.updateResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 404 },
			});
			(mockClient.createResume as ReturnType<typeof vi.fn>)
				.mockRejectedValueOnce({ response: { status: 500 } })
				.mockResolvedValueOnce(mockResponse);

			const result = await storage.save(mockResume);

			expect(mockClient.createResume).toHaveBeenCalledTimes(2);
			expect(result.data).toEqual(mockResume);
			expect(storage.getRetryCount()).toBe(0);
		});

		it("throws after max retries exceeded", async () => {
			config.resumeId = "test-resume-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.updateResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 503 },
			});

			await expect(storage.save(mockResume)).rejects.toThrow();
			expect(mockClient.updateResume).toHaveBeenCalledTimes(3);
		});

		it("persists resume ID to localStorage after successful save", async () => {
			(mockClient.updateResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 404 },
			});
			(mockClient.createResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				mockResponse,
			);

			await storage.save(mockResume);

			expect(localStorageMock.setItem).toHaveBeenCalledWith(
				"cvix:remote-resume-id:9dcb2241-6840-4e77-98a3-ddfa89c7d032",
				mockResponse.id,
			);
		});
	});

	describe("load", () => {
		it("returns null when no ID is configured and no resumes exist", async () => {
			(mockClient.listResumes as ReturnType<typeof vi.fn>).mockResolvedValue(
				[],
			);

			const result = await storage.load();

			expect(result.data).toBeNull();
			expect(mockClient.listResumes).toHaveBeenCalled();
			expect(mockClient.getResume).not.toHaveBeenCalled();
		});

		it("discovers and loads most recent resume when no ID configured", async () => {
			const olderResume: ResumeDocumentResponse = {
				...mockResponse,
				id: "older-id",
				updatedAt: "2024-01-01T00:00:00Z",
			};
			const newerResume: ResumeDocumentResponse = {
				...mockResponse,
				id: "newer-id",
				updatedAt: "2024-01-15T00:00:00Z",
			};

			(mockClient.listResumes as ReturnType<typeof vi.fn>).mockResolvedValue([
				olderResume,
				newerResume,
			]);
			(mockClient.getResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				newerResume,
			);

			const result = await storage.load();

			expect(mockClient.listResumes).toHaveBeenCalled();
			expect(mockClient.getResume).toHaveBeenCalledWith("newer-id");
			expect(result.data).toEqual(mockResume);
			expect(storage.getResumeId()).toBe("newer-id");
			// Verify localStorage persistence
			expect(localStorageMock.setItem).toHaveBeenCalledWith(
				"cvix:remote-resume-id:9dcb2241-6840-4e77-98a3-ddfa89c7d032",
				"newer-id",
			);
		});

		it("recovers resume ID from localStorage without calling listResumes", async () => {
			localStorageMock.getItem.mockReturnValue("persisted-id");
			(mockClient.getResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				mockResponse,
			);

			const result = await storage.load();

			expect(mockClient.listResumes).not.toHaveBeenCalled();
			expect(mockClient.getResume).toHaveBeenCalledWith("persisted-id");
			expect(result.data).toEqual(mockResume);
		});

		it("loads resume from server when id is provided", async () => {
			config.resumeId = "test-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.getResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				mockResponse,
			);

			const result = await storage.load();

			expect(mockClient.getResume).toHaveBeenCalledWith("test-id");
			expect(result.data).toEqual(mockResume);
			expect(storage.getResumeId()).toBe(mockResponse.id);
		});

		it("returns null on 404 instead of throwing", async () => {
			config.resumeId = "non-existent-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.getResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 404 },
			});

			const result = await storage.load();

			expect(result.data).toBeNull();
		});
		it("throws on non-404 errors after retries", async () => {
			config.resumeId = "test-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.getResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 500 },
			});

			await expect(storage.load()).rejects.toThrow();
			expect(mockClient.getResume).toHaveBeenCalledTimes(3); // maxRetries + 1
		});
	});

	describe("clear", () => {
		it("clears localStorage even when no ID exists in memory", async () => {
			await storage.clear();

			expect(mockClient.deleteResume).not.toHaveBeenCalled();
			expect(localStorageMock.removeItem).toHaveBeenCalledWith(
				"cvix:remote-resume-id:9dcb2241-6840-4e77-98a3-ddfa89c7d032",
			);
		});

		it("deletes resume from server and clears localStorage when id exists", async () => {
			config.resumeId = "test-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.deleteResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				undefined,
			);

			await storage.clear();

			expect(mockClient.deleteResume).toHaveBeenCalledWith("test-id");
			expect(storage.getResumeId()).toBeNull();
			expect(storage.getLastServerTimestamp()).toBeNull();
			expect(localStorageMock.removeItem).toHaveBeenCalledWith(
				"cvix:remote-resume-id:9dcb2241-6840-4e77-98a3-ddfa89c7d032",
			);
		});
	});

	describe("meta & type", () => {
		it("returns 'remote' as storage type", () => {
			expect(storage.type()).toBe("remote");
		});

		it("includes metadata in persistence result", async () => {
			(mockClient.updateResume as ReturnType<typeof vi.fn>).mockRejectedValue({
				response: { status: 404 },
			});
			(mockClient.createResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				mockResponse,
			);

			const result = await storage.save(mockResume);

			expect(result.metadata).toBeDefined();
			expect(result.metadata?.id).toBe(mockResponse.id);
			expect(result.metadata?.userId).toBe(mockResponse.userId);
			expect(result.metadata?.workspaceId).toBe(mockResponse.workspaceId);
		});
	});
});
