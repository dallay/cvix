import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import type {
	ResumeDocumentResponse,
	ResumeHttpClient,
} from "../http/ResumeHttpClient";
import {
	RemoteResumeStorage,
	type RemoteStorageConfig,
} from "./RemoteResumeStorage";

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

		mockClient = {
			createResume: vi.fn(),
			updateResume: vi.fn(),
			getResume: vi.fn(),
			deleteResume: vi.fn(),
		} as Partial<ResumeHttpClient> as ResumeHttpClient;

		config = {
			workspaceId: "workspace-id",
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
			expect(mockClient.createResume).toHaveBeenCalledWith(
				"test-resume-id",
				config.workspaceId,
				mockResume,
				undefined,
			);

			expect(result.data).toEqual(mockResume);
			expect(result.storageType).toBe("remote");
			expect(storage.getResumeId()).toBe(mockResponse.id);
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
	});

	describe("clear", () => {
		it("does nothing when no ID exists", async () => {
			await storage.clear();

			expect(mockClient.deleteResume).not.toHaveBeenCalled();
		});

		it("deletes resume from server when id exists", async () => {
			config.resumeId = "test-id";
			storage = new RemoteResumeStorage(config, mockClient);
			(mockClient.deleteResume as ReturnType<typeof vi.fn>).mockResolvedValue(
				undefined,
			);

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
