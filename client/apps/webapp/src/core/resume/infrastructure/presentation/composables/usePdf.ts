import { onScopeDispose, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";
import type {
	ParamValue,
	TemplateMetadata,
} from "@/core/resume/domain/TemplateMetadata";
import { resumeHttpClient } from "../../http/ResumeHttpClient";

export function usePdf() {
	const isGenerating = ref(false);
	const isLoadingTemplates = ref(true); // Start as true to prevent premature rendering
	const error = ref<string | null>(null);
	const templates = ref<TemplateMetadata[]>([]);
	const pdfUrl = ref<string | null>(null);
	// Cleanup on scope disposal
	onScopeDispose(() => {
		if (pdfUrl.value) {
			URL.revokeObjectURL(pdfUrl.value);
		}
	});

	const fetchTemplates = async (workspaceId: string) => {
		isLoadingTemplates.value = true;
		error.value = null;
		try {
			templates.value = await resumeHttpClient.getTemplates(workspaceId);
		} catch (e: unknown) {
			error.value = e instanceof Error ? e.message : "Failed to load templates";
		} finally {
			isLoadingTemplates.value = false;
		}
	};

	const generatePdf = async (
		resume: Resume,
		templateId: string,
		params: Record<string, ParamValue>,
	) => {
		isGenerating.value = true;
		error.value = null;

		// Revoke previous URL to avoid memory leaks
		if (pdfUrl.value) {
			URL.revokeObjectURL(pdfUrl.value);
			pdfUrl.value = null;
		}

		try {
			// Extract locale from params, default to 'en' if not present
			const rawLocale = params.locale;
			const locale: "en" | "es" = rawLocale === "es" ? "es" : "en";
			const blob = await resumeHttpClient.generatePdf(
				templateId,
				resume,
				locale,
			);
			pdfUrl.value = URL.createObjectURL(blob);
			return blob;
		} catch (e: unknown) {
			error.value = e instanceof Error ? e.message : "Failed to generate PDF";
			throw e;
		} finally {
			isGenerating.value = false;
		}
	};

	const downloadPdf = (blob: Blob, filename = "resume.pdf") => {
		const url = URL.createObjectURL(blob);
		const link = document.createElement("a");
		link.href = url;
		link.download = filename;
		document.body.appendChild(link);
		link.click();
		link.remove();
		URL.revokeObjectURL(url);
	};

	return {
		isGenerating,
		isLoadingTemplates,
		error,
		templates,
		pdfUrl,
		fetchTemplates,
		generatePdf,
		downloadPdf,
	};
}
