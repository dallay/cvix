// @ts-nocheck - Vitest module mocking with TypeScript is complex, tests pass in runtime
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

// Mock axios before importing BaseHttpClient
vi.mock("axios", () => {
	const mockAxiosInstance = {
		get: vi.fn(),
		post: vi.fn(),
		put: vi.fn(),
		patch: vi.fn(),
		delete: vi.fn(),
		interceptors: {
			request: {
				use: vi.fn(),
			},
			response: {
				use: vi.fn(),
			},
		},
	};
	return {
		default: {
			create: vi.fn(() => mockAxiosInstance),
		},
	};
});

// Mock WorkspaceContext
vi.mock("./WorkspaceContext", () => ({
	getCurrentWorkspaceId: vi.fn(),
}));

import axios from "axios";
import { BaseHttpClient } from "./BaseHttpClient";
import { getCurrentWorkspaceId } from "./WorkspaceContext";

describe("BaseHttpClient", () => {
	// biome-ignore lint/suspicious/noExplicitAny: Test mocking requires dynamic typing for interceptor capture
	let capturedRequestInterceptor: any;

	beforeEach(() => {
		vi.clearAllMocks();

		// Capture the request interceptor when it's registered
		const mockAxios = axios.create();
		vi.mocked(mockAxios.interceptors.request.use).mockImplementation(
			(onFulfilled) => {
				capturedRequestInterceptor = onFulfilled;
				return 0;
			},
		);
	});

	afterEach(() => {
		vi.resetAllMocks();
	});

	describe("workspace header injection", () => {
		it("should add X-Workspace-Id header when workspace is set", () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";
			vi.mocked(getCurrentWorkspaceId).mockReturnValue(workspaceId);

			// Create client to trigger interceptor registration
			new BaseHttpClient();

			// Simulate a request config
			const config = {
				headers: {},
			};

			const result = capturedRequestInterceptor(config);

			expect(result.headers["X-Workspace-Id"]).toBe(workspaceId);
		});

		it("should not add X-Workspace-Id header when workspace is null", () => {
			vi.mocked(getCurrentWorkspaceId).mockReturnValue(null);

			new BaseHttpClient();

			const config = {
				headers: {},
			};

			const result = capturedRequestInterceptor(config);

			expect(result.headers["X-Workspace-Id"]).toBeUndefined();
		});

		it("should not override existing X-Workspace-Id header", () => {
			const existingWorkspaceId = "existing-workspace-id";
			const newWorkspaceId = "550e8400-e29b-41d4-a716-446655440000";
			vi.mocked(getCurrentWorkspaceId).mockReturnValue(newWorkspaceId);

			new BaseHttpClient();

			const config = {
				headers: {
					"X-Workspace-Id": existingWorkspaceId,
				},
			};

			const result = capturedRequestInterceptor(config);

			expect(result.headers["X-Workspace-Id"]).toBe(existingWorkspaceId);
		});
	});

	describe("constructor", () => {
		it("should create client with default config", () => {
			new BaseHttpClient();

			expect(axios.create).toHaveBeenCalledWith(
				expect.objectContaining({
					baseURL: "/api",
					timeout: 10000,
					withCredentials: true,
				}),
			);
		});

		it("should use custom baseURL when provided", () => {
			new BaseHttpClient({ baseURL: "https://api.example.com" });

			expect(axios.create).toHaveBeenCalledWith(
				expect.objectContaining({
					baseURL: "https://api.example.com",
				}),
			);
		});

		it("should setup request and response interceptors", () => {
			const client = new BaseHttpClient();
			const mockAxios = client.getAxiosInstance();

			expect(mockAxios.interceptors.request.use).toHaveBeenCalled();
			expect(mockAxios.interceptors.response.use).toHaveBeenCalled();
		});
	});
});
