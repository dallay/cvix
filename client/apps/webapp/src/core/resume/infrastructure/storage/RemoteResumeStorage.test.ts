import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type { ResumeHttpClient } from "@/core/resume/infrastructure/http/ResumeHttpClient";
import {
	RemoteResumeStorage,
	type RemoteStorageConfig,
} from "./RemoteResumeStorage";

describe("RemoteResumeStorage", () => {
	let mockClient: ResumeHttpClient;
	let storage: RemoteResumeStorage;
	let config: RemoteStorageConfig;

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

	const mockResponse = {
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

		// Create a mock client matching the ResumeHttpClient API used by RemoteResumeStorage
		mockClient = {
			createResume: vi.fn(),
			getResume: vi.fn(),
			updateResume: vi.fn(),
			deleteResume: vi.fn(),
			// Other methods may exist but are not used in these tests
		} as unknown as ResumeHttpClient;

		config = {
			workspaceId: "workspace-id",
			initialRetryDelay: 0,
			maxRetryDelay: 0,
			maxRetries: 2,
		};

		storage = new RemoteResumeStorage(config, mockClient);
	});

	describe("save", () => {
		it("creates a new resume when update returns 404", async () => {
			// Simulate update throwing 404 so createResume is used
			(mockClient.updateResume as any).mockRejectedValueOnce({ status: 404 });
			(mockClient.createResume as any).mockResolvedValueOnce(mockResponse);

			const result = await storage.save(mockResume);

			expect(mockClient.updateResume).toHaveBeenCalled();
			expect(mockClient.createResume).toHaveBeenCalledWith(
				expect.any(String),
				config.workspaceId,
				mockResume,
				undefined,
			);

			expect(result.data).toBe(mockResume);
			expect(result.storageType).toBe("remote");
			expect(storage.getResumeId()).toBe(mockResponse.id);
		});

		it("updates existing resume when ID exists", async () => {
			// Start with an existing resume id
			config.resumeId = "existing-id";
			storage = new RemoteResumeStorage(config, mockClient);

			(mockClient.updateResume as any).mockResolvedValueOnce(mockResponse);

			await storage.save(mockResume);

			expect(mockClient.updateResume).toHaveBeenCalledWith(
				"existing-id",
				mockResume,
				undefined,
			);
		});

		it("retries and succeeds after transient failure", async () => {
			// For a new resume path, update will be attempted first and fail with 404, then create will be tried
			(mockClient.updateResume as any).mockRejectedValueOnce({ status: 404 });

			// Simulate transient failure on create then success
			(mockClient.createResume as any)
				.mockRejectedValueOnce(new Error("Network error"))
				.mockResolvedValueOnce(mockResponse);

			const result = await storage.save(mockResume);

			expect(
				(mockClient.createResume as any).mock.calls.length,
			).toBeGreaterThanOrEqual(1);
			expect(result.data).toBe(mockResume);
			expect(storage.getRetryCount()).toBe(0); // Reset after success
		});

		it("throws after max retries exceeded", async () => {
			// Force update to always fail (non-404) to trigger retries
			(mockClient.updateResume as any).mockRejectedValue(
				new Error("Permanent failure"),
			);

			await expect(storage.save(mockResume)).rejects.toThrow(/failed after/);

			// Should have attempted update (initial + retries)
			expect(
				(mockClient.updateResume as any).mock.calls.length,
			).toBeGreaterThanOrEqual(1);
		});
	});

	describe("load", () => {
		it("returns null when no ID is configured", async () => {
			const result = await storage.load();

			expect(result.data).toBeNull();
			expect(mockClient.getResume).not.toHaveBeenCalled();
		});

		it("loads resume from server when id is provided", async () => {
			config.resumeId = "test-id";
			storage = new RemoteResumeStorage(config, mockClient);

			(mockClient.getResume as any).mockResolvedValueOnce(mockResponse);

			const result = await storage.load();

			expect(mockClient.getResume).toHaveBeenCalledWith("test-id");
			expect(result.data).toEqual(mockResume);
			expect(storage.getResumeId()).toBe(mockResponse.id);
		});

		it("returns null on 404 instead of throwing", async () => {
			config.resumeId = "non-existent-id";
			storage = new RemoteResumeStorage(config, mockClient);

			(mockClient.getResume as any).mockRejectedValueOnce({ status: 404 });

			const result = await storage.load();

			expect(result.data).toBeNull();
		});
	});

	describe("clear", () => {
		it("does nothing when no ID exists", async () => {
			await storage.clear();

			expect(mockClient.deleteResume).not.toHaveBeenCalled();
		});

		it("deletes resume from server when id exists", async () => {
			config.resumeId = "test-id";
			storage = new RemoteResumeStorage(config, mockClient);

			(mockClient.deleteResume as any).mockResolvedValueOnce(undefined);

			await storage.clear();

			expect(mockClient.deleteResume).toHaveBeenCalledWith("test-id");
			expect(storage.getResumeId()).toBeNull();
			expect(storage.getLastServerTimestamp()).toBeNull();
		});
	});

	describe("meta & type", () => {
		it("returns 'remote' as storage type", () => {
			expect(storage.type()).toBe("remote");
		});

		it("includes metadata in persistence result", async () => {
			(mockClient.updateResume as any).mockRejectedValueOnce({ status: 404 });
			(mockClient.createResume as any).mockResolvedValueOnce(mockResponse);

			const result = await storage.save(mockResume);

			expect(result.metadata).toBeDefined();
			expect(result.metadata?.id).toBe(mockResponse.id);
			expect(result.metadata?.userId).toBe(mockResponse.userId);
			expect(result.metadata?.workspaceId).toBe(mockResponse.workspaceId);
		});
	});
});
