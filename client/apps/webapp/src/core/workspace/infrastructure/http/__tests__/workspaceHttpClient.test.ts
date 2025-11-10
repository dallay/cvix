import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Workspace } from "../../../domain/WorkspaceEntity.ts";
import type {
	GetAllWorkspacesResponse,
	GetWorkspaceResponse,
} from "../../../domain/WorkspaceError.ts";
import { WorkspaceHttpClient } from "../workspaceHttpClient.ts";

// Mock axios
vi.mock("axios", () => ({
	default: {
		create: vi.fn(() => ({
			interceptors: {
				request: { use: vi.fn() },
				response: { use: vi.fn() },
			},
			get: vi.fn(),
		})),
	},
}));

describe("WorkspaceHttpClient", () => {
	let client: WorkspaceHttpClient;

	beforeEach(() => {
		vi.clearAllMocks();
		client = new WorkspaceHttpClient();
	});

	describe("constructor", () => {
		it("should create client with default base URL", () => {
			const workspaceClient = new WorkspaceHttpClient();

			expect(workspaceClient).toBeInstanceOf(WorkspaceHttpClient);
		});

		it("should create client with custom config", () => {
			const customClient = new WorkspaceHttpClient({
				baseURL: "/custom-api/workspace",
				timeout: 5000,
			});

			expect(customClient).toBeInstanceOf(WorkspaceHttpClient);
		});
	});

	describe("getAllWorkspaces", () => {
		it("should fetch all workspaces from /workspace endpoint", async () => {
			const mockWorkspaces: Workspace[] = [
				{
					id: "550e8400-e29b-41d4-a716-446655440000",
					name: "Test Workspace",
					description: "A test workspace",
					isDefault: true,
					ownerId: "123e4567-e89b-42d3-a456-426614174000",
					createdAt: new Date("2025-10-01T10:00:00Z"),
					updatedAt: new Date("2025-10-01T10:00:00Z"),
				},
			];

			const mockResponse: GetAllWorkspacesResponse = {
				data: mockWorkspaces,
				meta: {
					total: 1,
					hasMore: false,
				},
			};

			// Mock the get method inherited from BaseHttpClient
			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			vi.spyOn(client as any, "get").mockResolvedValue(mockResponse);

			const result = await client.getAllWorkspaces();

			expect(result).toEqual(mockWorkspaces);
			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			expect((client as any).get).toHaveBeenCalledWith("/workspace");
		});

		it("should return empty array when no workspaces exist", async () => {
			const mockResponse: GetAllWorkspacesResponse = {
				data: [],
				meta: {
					total: 0,
					hasMore: false,
				},
			};

			// Mock the get method inherited from BaseHttpClient
			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			vi.spyOn(client as any, "get").mockResolvedValue(mockResponse);

			const result = await client.getAllWorkspaces();
			expect(result).toEqual([]);
		});

		it("should throw error when API call fails", async () => {
			const error = new Error("Network error");
			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			vi.spyOn(client as any, "get").mockRejectedValue(error);

			await expect(client.getAllWorkspaces()).rejects.toThrow("Network error");
		});
	});
	describe("getWorkspace", () => {
		it("should fetch specific workspace by ID", async () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";
			const mockWorkspace: Workspace = {
				id: workspaceId,
				name: "Test Workspace",
				description: "A test workspace",
				isDefault: true,
				ownerId: "123e4567-e89b-42d3-a456-426614174000",
				createdAt: new Date("2025-10-01T10:00:00Z"),
				updatedAt: new Date("2025-10-01T10:00:00Z"),
			};

			const mockResponse: GetWorkspaceResponse = {
				data: mockWorkspace,
			};

			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			vi.spyOn(client as any, "get").mockResolvedValue(mockResponse);

			const result = await client.getWorkspace(workspaceId);

			expect(result).toEqual(mockWorkspace);
			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			expect((client as any).get).toHaveBeenCalledWith(
				`/workspace/${workspaceId}`,
			);
		});

		it("should return null when workspace is not found", async () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";

			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			vi.spyOn(client as any, "get").mockResolvedValue(null);

			const result = await client.getWorkspace(workspaceId);

			expect(result).toBeNull();
		});

		it("should throw error when API call fails", async () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";
			const error = new Error("Network error");

			// biome-ignore lint/suspicious/noExplicitAny: Testing internal method requires type assertion
			vi.spyOn(client as any, "get").mockRejectedValue(error);

			await expect(client.getWorkspace(workspaceId)).rejects.toThrow(
				"Network error",
			);
		});
	});
});
