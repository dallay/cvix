import { readonly, ref } from "vue";

export interface ApiResponse<T = Record<string, unknown>> {
	success: boolean;
	data?: T;
	error?: string;
	statusCode?: number;
}

export interface EmailSubmissionData {
	email: string;
	source?: string;
	timestamp?: string;
	metadata?: Record<string, string | number | boolean>;
}

export function useEmailSubmission() {
	const isSubmitting = ref(false);
	const lastSubmission = ref<EmailSubmissionData | null>(null);
	const error = ref<string | null>(null);

	const submitEmail = async (
		email: string,
		options: {
			source?: string;
			metadata?: Record<string, string | number | boolean>;
			apiEndpoint?: string;
		} = {},
	): Promise<ApiResponse> => {
		isSubmitting.value = true;
		error.value = null;

		const submissionData: EmailSubmissionData = {
			email,
			source: options.source || "cta-email-form",
			timestamp: new Date().toISOString(),
			metadata: options.metadata,
		};

		try {
			// Simulate API call - replace with actual endpoint
			const response = await fetch(options.apiEndpoint || "/api/waitlist", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(submissionData),
			});

			if (!response.ok) {
				throw new Error(`HTTP ${response.status}: ${response.statusText}`);
			}

			const data = await response.json();
			lastSubmission.value = submissionData;

			return {
				success: true,
				data,
				statusCode: response.status,
			};
		} catch (err) {
			const errorMessage =
				err instanceof Error ? err.message : "Unknown error occurred";
			error.value = errorMessage;

			return {
				success: false,
				error: errorMessage,
				statusCode: 500,
			};
		} finally {
			isSubmitting.value = false;
		}
	};

	const resetSubmission = () => {
		isSubmitting.value = false;
		lastSubmission.value = null;
		error.value = null;
	};

	return {
		isSubmitting: readonly(isSubmitting),
		lastSubmission: readonly(lastSubmission),
		error: readonly(error),
		submitEmail,
		resetSubmission,
	};
}
