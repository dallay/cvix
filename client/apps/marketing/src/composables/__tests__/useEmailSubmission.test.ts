import { beforeEach, describe, expect, it, vi } from "vitest";
import { useEmailSubmission } from "../useEmailSubmission";

// Mock fetch globally
global.fetch = vi.fn();

describe("useEmailSubmission", () => {
	beforeEach(() => {
		vi.clearAllMocks();
		(fetch as unknown as ReturnType<typeof vi.fn>).mockClear();
	});

	it("should initialize with correct default values", () => {
		const { isSubmitting, lastSubmission, error } = useEmailSubmission();

		expect(isSubmitting.value).toBe(false);
		expect(lastSubmission.value).toBeNull();
		expect(error.value).toBeNull();
	});

	it("should submit email successfully", async () => {
		const mockResponse = {
			ok: true,
			status: 200,
			json: vi.fn().mockResolvedValue({ id: "123", message: "Success" }),
		};
		(fetch as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(
			mockResponse,
		);

		const { submitEmail, isSubmitting, lastSubmission } = useEmailSubmission();

		const result = await submitEmail("test@example.com", {
			source: "test-form",
		});

		expect(result.success).toBe(true);
		expect(result.data).toEqual({ id: "123", message: "Success" });
		expect(result.statusCode).toBe(200);
		expect(isSubmitting.value).toBe(false);
		expect(lastSubmission.value?.email).toBe("test@example.com");
		expect(lastSubmission.value?.source).toBe("test-form");
	});

	it("should handle API errors correctly", async () => {
		const mockResponse = {
			ok: false,
			status: 400,
			statusText: "Bad Request",
		};
		(fetch as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(
			mockResponse,
		);

		const { submitEmail, error } = useEmailSubmission();

		const result = await submitEmail("test@example.com");

		expect(result.success).toBe(false);
		expect(result.error).toBe("HTTP 400: Bad Request");
		expect(error.value).toBe("HTTP 400: Bad Request");
	});

	it("should handle network errors", async () => {
		(fetch as unknown as ReturnType<typeof vi.fn>).mockRejectedValue(
			new Error("Network error"),
		);

		const { submitEmail, error } = useEmailSubmission();

		const result = await submitEmail("test@example.com");

		expect(result.success).toBe(false);
		expect(result.error).toBe("Network error");
		expect(error.value).toBe("Network error");
	});

	it("should set loading state during submission", async () => {
		let resolvePromise: ((value: unknown) => void) | undefined;
		const mockPromise = new Promise((resolve) => {
			resolvePromise = resolve;
		});
		(fetch as unknown as ReturnType<typeof vi.fn>).mockReturnValue(mockPromise);

		const { submitEmail, isSubmitting } = useEmailSubmission();

		expect(isSubmitting.value).toBe(false);

		const submissionPromise = submitEmail("test@example.com");

		expect(isSubmitting.value).toBe(true);

		// Resolve the mock promise
		resolvePromise?.({
			ok: true,
			status: 200,
			json: () => Promise.resolve({ success: true }),
		});

		await submissionPromise;

		expect(isSubmitting.value).toBe(false);
	});

	it("should reset submission state", async () => {
		const mockResponse = {
			ok: false,
			status: 400,
			statusText: "Bad Request",
		};
		(fetch as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(
			mockResponse,
		);

		const {
			resetSubmission,
			submitEmail,
			isSubmitting,
			lastSubmission,
			error,
		} = useEmailSubmission();

		// First trigger an error state by making a failed submission
		await submitEmail("test@example.com");

		// Verify error state is set
		expect(error.value).toBe("HTTP 400: Bad Request");
		expect(lastSubmission.value).toBeNull();

		// Now reset
		resetSubmission();

		expect(isSubmitting.value).toBe(false);
		expect(lastSubmission.value).toBeNull();
		expect(error.value).toBeNull();
	});

	it("should include metadata in submission", async () => {
		const mockResponse = {
			ok: true,
			status: 200,
			json: vi.fn().mockResolvedValue({ success: true }),
		};
		(fetch as unknown as ReturnType<typeof vi.fn>).mockResolvedValue(
			mockResponse,
		);

		const { submitEmail } = useEmailSubmission();

		await submitEmail("test@example.com", {
			source: "newsletter",
			metadata: { campaign: "summer-2024", utm_source: "google" },
		});

		expect(fetch).toHaveBeenCalledWith("/api/waitlist", {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: expect.stringContaining(
				'"metadata":{"campaign":"summer-2024","utm_source":"google"}',
			),
		});
	});
});
