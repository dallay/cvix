import type { InternalAxiosRequestConfig } from "axios";
import type { Mock } from "vitest";
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

// Mock CsrfService
vi.mock("./csrf.service", () => ({
	csrfService: {
		isInitialized: vi.fn(),
		initialize: vi.fn(),
	},
}));

import axios from "axios";
import { BaseHttpClient } from "./BaseHttpClient";
import { csrfService } from "./csrf.service";
import { getCurrentWorkspaceId } from "./WorkspaceContext";

describe("BaseHttpClient", () => {
	let capturedRequestInterceptor: (
		config: InternalAxiosRequestConfig,
	) => Promise<InternalAxiosRequestConfig>;

	beforeEach(() => {
		vi.resetAllMocks();

		// Capture the request interceptor when it's registered
		const mockAxios = (axios.create as Mock)();
		(mockAxios.interceptors.request.use as Mock).mockImplementation(
			(
				onFulfilled: (
					config: InternalAxiosRequestConfig,
				) => Promise<InternalAxiosRequestConfig>,
			) => {
				capturedRequestInterceptor = onFulfilled;
				return 0;
			},
		);
	});

	afterEach(() => {
		vi.resetAllMocks();
	});

	describe("workspace header injection", () => {
		it("should add X-Workspace-Id header when workspace is set", async () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";
			(getCurrentWorkspaceId as Mock).mockReturnValue(workspaceId);
			(csrfService.isInitialized as Mock).mockReturnValue(true);

			// Create client to trigger interceptor registration
			new BaseHttpClient();

			// Simulate a request config
			const config = {
				headers: {},
			} as unknown as InternalAxiosRequestConfig;

			const result = await capturedRequestInterceptor(config);

			expect(result.headers["X-Workspace-Id"]).toBe(workspaceId);
		});

		it("should not add X-Workspace-Id header when workspace is null", async () => {
			(getCurrentWorkspaceId as Mock).mockReturnValue(null);
			(csrfService.isInitialized as Mock).mockReturnValue(true);

			new BaseHttpClient();

			const config = {
				headers: {},
			} as unknown as InternalAxiosRequestConfig;

			const result = await capturedRequestInterceptor(config);

			expect(result.headers["X-Workspace-Id"]).toBeUndefined();
		});

		it("should not override existing X-Workspace-Id header", async () => {
			const existingWorkspaceId = "existing-workspace-id";
			const newWorkspaceId = "550e8400-e29b-41d4-a716-446655440000";
			(getCurrentWorkspaceId as Mock).mockReturnValue(newWorkspaceId);
			(csrfService.isInitialized as Mock).mockReturnValue(true);

			new BaseHttpClient();

			const config = {
				headers: {
					"X-Workspace-Id": existingWorkspaceId,
				},
			} as unknown as InternalAxiosRequestConfig;

			const result = await capturedRequestInterceptor(config);

			expect(result.headers["X-Workspace-Id"]).toBe(existingWorkspaceId);
		});

		it("should handle missing headers object in config gracefully", async () => {
			const workspaceId = "550e8400-e29b-41d4-a716-446655440000";
			(getCurrentWorkspaceId as Mock).mockReturnValue(workspaceId);
			(csrfService.isInitialized as Mock).mockReturnValue(true);

			new BaseHttpClient();

			// Simulate a request config with NO headers property
			const config = {} as unknown as InternalAxiosRequestConfig;

			const result = await capturedRequestInterceptor(config);

			expect(result.headers).toBeDefined();
			expect(result.headers["X-Workspace-Id"]).toBe(workspaceId);
		});
	});

	describe("CSRF initialization", () => {
		it("should await CSRF initialization if not already initialized", async () => {
			(csrfService.isInitialized as Mock).mockReturnValue(false);
			(csrfService.initialize as Mock).mockResolvedValue(undefined);

			new BaseHttpClient();

			const config = {
				headers: {},
			} as unknown as InternalAxiosRequestConfig;

			await capturedRequestInterceptor(config);

			expect(csrfService.isInitialized).toHaveBeenCalledOnce();
			expect(csrfService.initialize).toHaveBeenCalledOnce();
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
