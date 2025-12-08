<script setup lang="ts">
import { useDebounceFn } from "@vueuse/core";
import { ArrowLeft, Download, Loader2 } from "lucide-vue-next";
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import type {
	ArraySectionType,
	SectionType,
} from "@/core/resume/domain/SectionVisibility";
import type { ParamValue } from "@/core/resume/domain/TemplateMetadata";
import DashboardLayout from "@/layouts/DashboardLayout.vue";
import { useResumeStore } from "../../store/resume.store";
import { useSectionVisibilityStore } from "../../store/section-visibility.store";
import PdfTemplateSelector from "../components/PdfTemplateSelector.vue";
import SectionTogglePanel from "../components/SectionTogglePanel.vue";
import { usePdf } from "../composables/usePdf";

const router = useRouter();
const resumeStore = useResumeStore();
const visibilityStore = useSectionVisibilityStore();
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
	params: {} as Record<string, ParamValue>,
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

	// Initialize section visibility
	if (resumeStore.resume) {
		visibilityStore.initialize(resumeStore.resume);
	}

	// Select first template by default if available
	if (templates.value.length > 0 && !selectedTemplate.value.templateId) {
		const firstTemplate = templates.value[0];
		if (firstTemplate) {
			selectedTemplate.value.templateId = firstTemplate.id;
		}
	}
});

const handleGeneratePdf = async (): Promise<Blob> => {
	if (!selectedTemplate.value.templateId) {
		throw new Error("No template selected");
	}

	// Use filtered resume if available, otherwise use the full resume
	const resumeToGenerate = visibilityStore.filteredResume || resumeStore.resume;
	if (!resumeToGenerate) {
		throw new Error("No resume data available");
	}

	try {
		const result = await generatePdf(
			resumeToGenerate,
			selectedTemplate.value.templateId,
			selectedTemplate.value.params,
		);
		return result as Blob;
	} catch (e) {
		console.error("PDF generation failed", e);
		throw e;
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

// Watch for visibility changes to regenerate preview
watch(
	() => visibilityStore.filteredResume,
	(filteredResume) => {
		if (selectedTemplate.value.templateId && filteredResume) {
			debouncedGenerate();
		}
	},
);

const onDownload = async () => {
	if (!selectedTemplate.value.templateId) return;

	const resumeToDownload = visibilityStore.filteredResume || resumeStore.resume;
	if (!resumeToDownload) return;

	try {
		const blob = await generatePdf(
			resumeToDownload,
			selectedTemplate.value.templateId,
			selectedTemplate.value.params,
		);
		downloadPdf(blob);
	} catch (e) {
		console.error("PDF download failed", e);
	}
};

const handleToggleSection = (section: SectionType) => {
	visibilityStore.toggleSection(section);
};

const handleExpandSection = (section: SectionType) => {
	visibilityStore.toggleSectionExpanded(section);
};

const handleToggleItem = (section: ArraySectionType, index: number) => {
	visibilityStore.toggleItem(section, index);
};

const handleToggleField = (field: string) => {
	visibilityStore.togglePersonalDetailsField(field);
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
        <!-- Sidebar: Template Selection & Appearance Settings -->
        <div class="w-[360px] border-r bg-card overflow-y-auto">
          <div class="p-6">
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
        </div>
        <!-- Main Area: Section Toggles and Preview -->
        <div class="flex-1 bg-muted/30 p-8 flex flex-col overflow-hidden">
          <!-- Section Toggle Panel -->
          <div v-if="resumeStore.resume && visibilityStore.visibility" class="mb-6 pb-6 border-b">
            <h2 class="text-sm font-semibold text-foreground mb-4">
              {{ t('resume.pdfPage.visibleSections', 'Visible Sections') }}
            </h2>
            <SectionTogglePanel
                :resume="resumeStore.resume"
                :visibility="visibilityStore.visibility"
                :metadata="visibilityStore.sectionMetadata"
                @toggle-section="handleToggleSection"
                @expand-section="handleExpandSection"
                @toggle-item="handleToggleItem"
                @toggle-field="handleToggleField"
            />
          </div>

          <!-- PDF Preview -->
          <div class="flex-1 flex flex-col items-center justify-center overflow-hidden relative">
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
    </div>
  </DashboardLayout>
</template>
