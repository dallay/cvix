/**
 * Mock API handlers for development and testing
 * This simulates the backend API responses for email submissions
 */

export interface MockApiConfig {
	delay?: number;
	successRate?: number;
	responses?: {
		success?: Record<string, unknown>;
		error?: Record<string, unknown>;
	};
}

export class MockEmailApi {
	private config: MockApiConfig;

	constructor(config: MockApiConfig = {}) {
		this.config = {
			delay: 800,
			successRate: 0.9,
			responses: {
				success: {
					email: "",
					status: "subscribed",
					createdAt: new Date().toISOString(),
					source: "marketing",
				},
				error: {
					code: "VALIDATION_ERROR",
					message: "Invalid email address",
					details: {},
				},
			},
			...config,
		};
	}

	private generateId(): string {
		return `sub_${Math.random().toString(36).substring(2, 11)}`;
	}

	private delay(ms: number): Promise<void> {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	async submitEmail(
		email: string,
		metadata?: Record<string, string | number | boolean>,
	): Promise<{
		ok: boolean;
		status: number;
		json: () => Promise<Record<string, unknown>>;
	}> {
		// Simulate network delay
		await this.delay(this.config.delay || 800);

		// Simulate random failures based on success rate
		const shouldSucceed = Math.random() < (this.config.successRate || 0.9);

		if (shouldSucceed) {
			return {
				ok: true,
				status: 201,
				json: async () => ({
					...this.config.responses?.success,
					id: this.generateId(),
					email,
					metadata: metadata || {},
					createdAt: new Date().toISOString(),
				}),
			};
		}
		return {
			ok: false,
			status: 400,
			json: async () => ({
				...this.config.responses?.error,
				details: {
					email,
					timestamp: new Date().toISOString(),
				},
			}),
		};
	}

	// Preset scenarios for testing
	static scenarios = {
		alwaysSuccess: new MockEmailApi({ successRate: 1.0, delay: 500 }),
		alwaysFail: new MockEmailApi({ successRate: 0.0, delay: 300 }),
		slow: new MockEmailApi({ delay: 3000 }),
		fast: new MockEmailApi({ delay: 100 }),
	};
}

// Global mock setup for fetch in development
export function setupMockApi(config?: MockApiConfig) {
	if (typeof window !== "undefined" && process.env.NODE_ENV === "development") {
		const mockApi = new MockEmailApi(config);

		const originalFetch = window.fetch;

		window.fetch = async (
			url: string | URL | Request,
			options?: RequestInit,
		): Promise<Response> => {
			const urlString = url.toString();

			// Intercept waitlist API calls
			if (urlString.includes("/api/waitlist") && options?.method === "POST") {
				try {
					const body = JSON.parse(options.body as string);
					const mockResponseData = await mockApi.submitEmail(
						body.email,
						body.metadata,
					);
					const responseBody = await mockResponseData.json();
					return new Response(JSON.stringify(responseBody), {
						status: mockResponseData.status,
						headers: { "Content-Type": "application/json" },
					});
				} catch (_error) {
					const errorResponse = { error: "Invalid request body" };
					return new Response(JSON.stringify(errorResponse), {
						status: 500,
						headers: { "Content-Type": "application/json" },
					});
				}
			}

			// Pass through other requests to original fetch
			return originalFetch(url, options);
		};

		console.log("🚀 Mock Email API enabled for development");
		// return mockApi // This return is not strictly necessary if not used elsewhere after setup
	}
}

// Utility for testing specific email validation scenarios
export const emailTestCases = {
	valid: [
		"user@example.com",
		"test.email@domain.co.uk",
		"user+tag@example.org",
		"firstname.lastname@company.com",
	],
	invalid: [
		"invalid-email",
		"user@",
		"@domain.com",
		"user..double.dot@example.com",
		"user@domain",
		"user name@example.com",
	],
	edge: [
		"a@b.co",
		"very.long.email.address.that.should.still.work@very.long.domain.name.that.is.still.valid.com",
		"user+multiple+tags@example.com",
		"user.with-dash@example-domain.com",
	],
};
