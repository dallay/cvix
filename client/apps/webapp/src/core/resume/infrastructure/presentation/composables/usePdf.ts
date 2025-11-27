import { onScopeDispose, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";
import { resumeHttpClient } from "../../http/ResumeHttpClient";

export function usePdf() {
	const isGenerating = ref(false);
	const isLoadingTemplates = ref(false);
	const error = ref<string | null>(null);
	const templates = ref<TemplateMetadata[]>([]);
	const pdfUrl = ref<string | null>(null);
	// Cleanup on scope disposal
	onScopeDispose(() => {
		if (pdfUrl.value) {
			URL.revokeObjectURL(pdfUrl.value);
		}
	});

	const fetchTemplates = async () => {
		isLoadingTemplates.value = true;
		error.value = null;
		try {
			templates.value = await resumeHttpClient.getTemplates();
		} catch (e: unknown) {
			error.value = e instanceof Error ? e.message : "Failed to load templates";
		} finally {
			isLoadingTemplates.value = false;
		}
	};

	const generatePdf = async (
		resume: Resume,
		_templateId: string,
		_params: Record<string, any>,
	) => {
		isGenerating.value = true;
		error.value = null;

		// Revoke previous URL to avoid memory leaks
		if (pdfUrl.value) {
			URL.revokeObjectURL(pdfUrl.value);
			pdfUrl.value = null;
		}

		try {
			// Note: Backend currently doesn't support templateId/params in generate endpoint yet
			// We are passing them but they might be ignored until backend is updated to use them
			// For now, we just call the existing endpoint
			const blob = await resumeHttpClient.generatePdf(resume);
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
		link.remove(); // Use the modern remove() method on the node instead of calling removeChild on its parent
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
