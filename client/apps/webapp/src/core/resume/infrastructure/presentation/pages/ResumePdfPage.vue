<script setup lang="ts">
import { Button } from "@cvix/ui/components/ui/button";
import { ScrollArea } from "@cvix/ui/components/ui/scroll-area";
import { useDebounceFn } from "@vueuse/core";
import { ArrowLeft, Download, Loader2, ZoomIn, ZoomOut } from "lucide-vue-next";
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import type {
	ArraySectionType,
	SectionType,
} from "@/core/resume/domain/SectionVisibility";
import type { ParamValue } from "@/core/resume/domain/TemplateMetadata";
import { useWorkspaceStore } from "@/core/workspace";
import DashboardLayout from "@/layouts/DashboardLayout.vue";
import { useResumeStore } from "../../store/resume.store";
import { useSectionVisibilityStore } from "../../store/section-visibility.store";
import PdfTemplateSelector from "../components/PdfTemplateSelector.vue";
import ResumePreviewSkeleton from "../components/ResumePreviewSkeleton.vue";
import SectionTogglePanel from "../components/SectionTogglePanel.vue";
import { usePdf } from "../composables/usePdf";

// PDF preview zoom state
const previewScale = ref(1.0);
const MIN_SCALE = 0.5;
const MAX_SCALE = 1.5;
const SCALE_STEP = 0.1;

const zoomIn = () => {
	if (previewScale.value < MAX_SCALE) {
		previewScale.value = Math.min(MAX_SCALE, previewScale.value + SCALE_STEP);
	}
};

const zoomOut = () => {
	if (previewScale.value > MIN_SCALE) {
		previewScale.value = Math.max(MIN_SCALE, previewScale.value - SCALE_STEP);
	}
};

const zoomPercentage = computed(() => Math.round(previewScale.value * 100));

const router = useRouter();
const resumeStore = useResumeStore();
const visibilityStore = useSectionVisibilityStore();
const workspaceStore = useWorkspaceStore();
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

	const workspaceId = workspaceStore?.currentWorkspace?.id;
	if (!workspaceId) {
		console.error("No workspace ID found");
		pdfError.value = t(
			"resume.pdfPage.error",
			"No workspace selected. Please select a workspace first.",
		);
		isLoadingTemplates.value = false;
		return;
	}
	// Fetch available templates (workspaceId is sent via X-Workspace-Id header)
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
	() => visibilityStore.visibility,
	() => {
		if (selectedTemplate.value.templateId && visibilityStore.filteredResume) {
			debouncedGenerate();
		}
	},
	{ deep: true },
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
    <div class="flex h-[calc(100vh-4rem)] overflow-hidden bg-background text-foreground">
      <!-- Left Sidebar - All Controls -->
      <aside class="w-[420px] flex flex-col border-r border-border bg-card shadow-lg z-10">
        <!-- Sidebar Header -->
        <div class="p-6 border-b border-border">
          <button
            class="flex items-center gap-2 text-muted-foreground hover:text-foreground cursor-pointer mb-4 transition-colors w-fit text-sm font-medium"
            @click="goBack"
          >
            <ArrowLeft class="h-4 w-4" />
            <span>{{ t('resume.pdfPage.backToEditor', 'Back to Editor') }}</span>
          </button>
          <h1 class="text-2xl font-bold text-foreground tracking-tight">
            {{ t('resume.pdfPage.title', 'Generate PDF') }}
          </h1>
        </div>

        <!-- Scrollable Content Area -->
        <div class="flex-1 overflow-y-auto min-h-0">
          <div class="p-6 space-y-8">
            <!-- Loading State -->
            <div v-if="isLoadingTemplates" class="flex justify-center py-8">
              <Loader2 class="h-6 w-6 animate-spin text-muted-foreground" />
            </div>

            <!-- Error State -->
            <div
              v-else-if="pdfError"
              class="rounded-md bg-destructive/10 p-4 text-sm text-destructive"
            >
              {{ pdfError }}
            </div>

            <!-- Template Selector & Options -->
            <PdfTemplateSelector
              v-else
              v-model="selectedTemplate"
              :templates="templates"
            />

            <!-- Section Visibility Controls -->
            <div v-if="resumeStore.resume && visibilityStore.visibility && !isLoadingTemplates" class="space-y-4">
              <div class="flex items-center justify-between">
                <h3 class="text-sm font-medium text-muted-foreground">
                  {{ t('resume.pdfPage.contentSelection', 'Content Selection') }}
                </h3>
                <span class="text-xs text-primary font-medium">
                  {{ t('resume.pdfPage.customizeRole', 'Customize for this role') }}
                </span>
              </div>

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
          </div>
        </div>

        <!-- Sidebar Footer - Download Action -->
        <div class="p-6 border-t border-border bg-card">
          <Button
            :disabled="isGenerating || !selectedTemplate.templateId"
            class="w-full"
            size="lg"
            @click="onDownload"
          >
            <Loader2 v-if="isGenerating" class="h-4 w-4 animate-spin mr-2" />
            <Download v-else class="h-4 w-4 mr-2" />
            {{ t('resume.pdfPage.download', 'Download PDF') }}
          </Button>
        </div>
      </aside>

      <!-- Main Area - PDF Preview -->
      <main class="flex-1 bg-muted/30 relative overflow-hidden flex flex-col">
        <!-- Preview Toolbar -->
        <div class="h-14 border-b border-border flex items-center justify-between px-6 bg-background/50 backdrop-blur-sm">
          <div class="flex items-center gap-2">
            <span class="text-sm text-muted-foreground">
              {{ t('resume.pdfPage.previewMode', 'Preview Mode') }}
            </span>
          </div>
          <div class="flex items-center gap-2">
            <div class="flex items-center bg-muted rounded-md border border-border p-1">
              <button
                :disabled="previewScale <= MIN_SCALE"
                class="px-2 py-1 text-xs text-muted-foreground hover:text-foreground hover:bg-background rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                @click="zoomOut"
              >
                <ZoomOut class="h-3.5 w-3.5" />
              </button>
              <span class="px-3 text-xs text-foreground min-w-[48px] text-center">
                {{ zoomPercentage }}%
              </span>
              <button
                :disabled="previewScale >= MAX_SCALE"
                class="px-2 py-1 text-xs text-muted-foreground hover:text-foreground hover:bg-background rounded disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                @click="zoomIn"
              >
                <ZoomIn class="h-3.5 w-3.5" />
              </button>
            </div>
          </div>
        </div>

        <!-- Preview Canvas -->
        <div class="flex-1 overflow-auto p-8 flex justify-center">
          <div class="relative">
            <!-- Generating Overlay -->
            <div
              v-if="isGenerating && !pdfPreviewUrl"
              class="absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm z-10 rounded-lg"
            >
              <div class="flex flex-col items-center gap-3">
                <Loader2 class="h-8 w-8 animate-spin text-primary" />
                <p class="text-sm text-muted-foreground">
                  {{ t('resume.pdfPage.generatingPreview', 'Generating preview...') }}
                </p>
              </div>
            </div>

            <!-- PDF Preview -->
            <div
              v-else-if="pdfPreviewUrl"
              class="origin-top transition-transform duration-300 ease-in-out"
              :style="{ transform: `scale(${previewScale})` }"
            >
              <object
                :data="pdfPreviewUrl"
                class="rounded-lg shadow-2xl border border-border bg-white"
                style="width: 210mm; height: 297mm;"
                type="application/pdf"
              >
                <div class="flex h-full items-center justify-center text-muted-foreground p-8">
                  <p class="text-sm text-center">
                    {{ t('resume.pdfPage.unablePreview', 'Unable to display PDF preview. Please download to view.') }}
                  </p>
                </div>
              </object>
            </div>

            <!-- Skeleton Preview (waiting for PDF generation) -->
            <div
              v-else
              class="origin-top transition-transform duration-300 ease-in-out relative"
              :style="{ transform: `scale(${previewScale})` }"
            >
              <ResumePreviewSkeleton />
              <!-- Generating overlay on skeleton -->
              <div
                v-if="isGenerating"
                class="absolute inset-0 flex items-center justify-center bg-background/60 backdrop-blur-[2px] rounded-lg"
              >
                <div class="flex flex-col items-center gap-3">
                  <Loader2 class="h-8 w-8 animate-spin text-primary" />
                  <p class="text-sm text-muted-foreground">
                    {{ t('resume.pdfPage.generatingPreview', 'Generating preview...') }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </DashboardLayout>
</template>
