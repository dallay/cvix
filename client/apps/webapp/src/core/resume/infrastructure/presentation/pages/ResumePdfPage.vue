<script setup lang="ts">
import { useDebounceFn } from "@vueuse/core";
import { ArrowLeft, Download, Loader2 } from "lucide-vue-next";
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import DashboardLayout from "@/layouts/DashboardLayout.vue";
import { useResumeStore } from "../../store/resume.store";
import PdfTemplateSelector from "../components/PdfTemplateSelector.vue";
import { usePdf } from "../composables/usePdf";

const router = useRouter();
const resumeStore = useResumeStore();
const { t } = useI18n();
const {
	templates,
	isLoadingTemplates,
	isGenerating,
	error: pdfError,
	pdfUrl,
	fetchTemplates,
	generatePdf,
	downloadPdf,
} = usePdf();

const selectedTemplate = ref({
	templateId: "",
	params: {} as Record<string, any>,
});

const pdfPreviewUrl = computed(() => {
	if (!pdfUrl.value) return null;
	// #navpanes=0 hides the sidebar (thumbnails)
	// #toolbar=0 hides the toolbar (optional, but cleaner for preview)
	// #view=FitH fits the page width
	return `${pdfUrl.value}#navpanes=0&toolbar=0&view=FitH`;
});

// Redirect if no resume data
onMounted(async () => {
	if (!resumeStore.hasResume) {
		// Try to load from storage if not in memory
		try {
			await resumeStore.loadFromStorage();
			if (!resumeStore.hasResume) {
				await router.push("/resume/editor");
				return;
			}
		} catch (e) {
			await router.push("/resume/editor");
			return;
		}
	}

	await fetchTemplates();

	// Select first template by default if available
	if (templates.value.length > 0 && !selectedTemplate.value.templateId) {
		const firstTemplate = templates.value[0];
		if (firstTemplate) {
			selectedTemplate.value.templateId = firstTemplate.id;
		}
	}
});

const handleGeneratePdf = async () => {
	if (!resumeStore.resume || !selectedTemplate.value.templateId) return;

	try {
		await generatePdf(
			resumeStore.resume,
			selectedTemplate.value.templateId,
			selectedTemplate.value.params,
		);
	} catch (e) {
		console.error("PDF generation failed", e);
	}
};

const debouncedGenerate = useDebounceFn(handleGeneratePdf, 500);

// Watch for template or param changes to regenerate preview
watch(
	selectedTemplate,
	() => {
		if (selectedTemplate.value.templateId) {
			debouncedGenerate();
		}
	},
	{ deep: true },
);

const onDownload = async () => {
	if (pdfUrl.value) {
		// If we already have a URL, use it (assuming it matches current state)
		// But to be safe and ensure we download exactly what's selected, we might want to regenerate or just download the blob.
		// Since generatePdf returns the blob, we can use that.
		// For now, let's use the existing blob if we're not generating.

		// However, we need the blob object to download. usePdf exposes downloadPdf which takes a blob.
		// But we only have pdfUrl exposed.
		// Let's modify logic: if we have a valid preview, we can fetch the blob from the URL or re-generate.
		// Re-generating ensures we get the latest.

		await handleGeneratePdf();
		// We need the blob. generatePdf returns it.
		// Let's call it directly.
		if (resumeStore.resume) {
			const blob = await generatePdf(
				resumeStore.resume,
				selectedTemplate.value.templateId,
				selectedTemplate.value.params,
			);
			downloadPdf(blob);
		}
	}
};

const goBack = async () => {
	await router.push("/resume/editor");
};
</script>

<template>
  <DashboardLayout>
    <div class="flex h-[calc(100vh-4rem)] flex-col">
      <!-- Toolbar -->
      <div class="border-b bg-background px-6 py-3 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <button
              class="inline-flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
              @click="goBack"
          >
            <ArrowLeft class="h-4 w-4"/>
            {{ t('resume.pdfPage.backToEditor', 'Back to Editor') }}
          </button>
          <div class="h-4 w-px bg-border"></div>
          <h1 class="text-lg font-semibold">{{ t('resume.pdfPage.title', 'Generate PDF') }}</h1>
        </div>
        <button
            :disabled="isGenerating || !selectedTemplate.templateId"
            class="inline-flex items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground shadow hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
            @click="onDownload"
        >
          <Loader2 v-if="isGenerating" class="h-4 w-4 animate-spin"/>
          <Download v-else class="h-4 w-4"/>
          {{ t('resume.pdfPage.download', 'Download PDF') }}
        </button>
      </div>
      <div class="flex flex-1 overflow-hidden">
        <!-- Sidebar: Template Selection -->
        <div class="w-80 border-r bg-muted/10 overflow-y-auto p-6">
          <div v-if="isLoadingTemplates" class="flex justify-center py-8">
            <Loader2 class="h-6 w-6 animate-spin text-muted-foreground"/>
          </div>
          <div v-else-if="pdfError"
               class="rounded-md bg-destructive/10 p-4 text-sm text-destructive">
            {{ pdfError }}
          </div>
          <PdfTemplateSelector
              v-else
              v-model="selectedTemplate"
              :templates="templates"
          />
        </div>
        <!-- Main Area: Preview -->
        <div
            class="flex-1 bg-muted/30 p-8 flex flex-col items-center justify-center overflow-hidden relative">
          <div v-if="isGenerating && !pdfUrl"
               class="absolute inset-0 flex items-center justify-center bg-background/50 z-10">
            <div class="flex flex-col items-center gap-2">
              <Loader2 class="h-8 w-8 animate-spin text-primary"/>
              <p class="text-sm text-muted-foreground">
                {{ t('resume.pdfPage.generatingPreview', 'Generating preview...') }}</p>
            </div>
          </div>
          <div v-if="!selectedTemplate.templateId" class="text-center text-muted-foreground">
            <p>{{ t('resume.pdfPage.selectTemplate', 'Select a template to generate preview') }}</p>
          </div>
          <object
              v-else-if="pdfPreviewUrl"
              :data="pdfPreviewUrl"
              class="w-full h-full rounded-lg shadow-lg border bg-white max-w-[210mm]"
              type="application/pdf"
          >
            <div class="flex h-full items-center justify-center text-muted-foreground">
              <p>{{
                  t('resume.pdfPage.unablePreview', 'Unable to display PDF preview. Please download to view.')
                }}</p>
            </div>
          </object>
        </div>
      </div>
    </div>
  </DashboardLayout>
</template>
