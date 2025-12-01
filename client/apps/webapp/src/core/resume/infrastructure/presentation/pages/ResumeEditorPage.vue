<script setup lang="ts">
import {
	AlertDialog,
	AlertDialogAction,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@cvix/ui/components/ui/alert-dialog";
import { Button } from "@cvix/ui/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@cvix/ui/components/ui/card";
import { useMagicKeys } from "@vueuse/core";
import {
	CheckCircle,
	Download,
	FileText,
	RotateCcw,
	Upload,
} from "lucide-vue-next";
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { toast } from "vue-sonner";
import ResumeForm from "@/core/resume/infrastructure/presentation/components/ResumeForm.vue";
import ResumePreview from "@/core/resume/infrastructure/presentation/components/ResumePreview.vue";
import ValidationErrorPanel from "@/core/resume/infrastructure/presentation/components/ValidationErrorPanel.vue";
import { useJsonResume } from "@/core/resume/infrastructure/presentation/composables/useJsonResume";
import { useResumeForm } from "@/core/resume/infrastructure/presentation/composables/useResumeForm";
import { useResumeStore } from "@/core/resume/infrastructure/store/resume.store";
import DashboardLayout from "@/layouts/DashboardLayout.vue";

const { t } = useI18n();
const router = useRouter();
const showPreview = ref(true);
const showValidationPanel = ref(false);
const showUploadConfirmation = ref(false);
const showResetConfirmation = ref(false);
const fileInputRef = ref<HTMLInputElement | null>(null);
const pendingFile = ref<File | null>(null);

// Get the resume data from the form composable
const { resume, loadResume: setResume, clearForm } = useResumeForm();

// Get store for accessing storage timestamps
const resumeStore = useResumeStore();

// JSON Resume import/export
const { importJson, exportJson, validateResume, validationErrors } =
	useJsonResume();

// Track if data has been modified since last save
const hasUnsavedChanges = ref(false);
const isInitialLoad = ref(true);
watch(
	resume,
	() => {
		if (isInitialLoad.value) {
			isInitialLoad.value = false;
			return;
		}
		hasUnsavedChanges.value = true;
	},
	{ deep: true },
);

// Format last saved timestamp
const lastSavedText = computed(() => {
	// TODO: Implement timestamp tracking when autosave/persistence is connected
	// For now, return placeholder
	return null;
});

// Prevent browser default Save dialog on Cmd/Ctrl+S
function handleSaveShortcut(event: KeyboardEvent) {
	if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "s") {
		event.preventDefault();
		event.stopPropagation();
		handleExportJson();
	}
}

// Warn user about unsaved changes before navigating away
function handleBeforeUnload(event: BeforeUnloadEvent) {
	if (hasUnsavedChanges.value && resume.value) {
		// Modern browsers display a generic message, but we still need to set returnValue
		event.preventDefault();
		event.returnValue = "";
		return "";
	}
}

onMounted(() => {
	window.addEventListener("keydown", handleSaveShortcut);
	window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
	window.removeEventListener("keydown", handleSaveShortcut);
	window.removeEventListener("beforeunload", handleBeforeUnload);
});

// Keyboard shortcuts
const keys = useMagicKeys();
const cmdS = keys["Meta+KeyS"];
const ctrlS = keys["Ctrl+KeyS"];

// Handle Cmd/Ctrl+S for export
watch([cmdS, ctrlS], ([cmd, ctrl]) => {
	if (cmd || ctrl) {
		handleExportJson();
	}
});

const togglePreview = () => {
	showPreview.value = !showPreview.value;
};

/**
 * Triggers the hidden file input for upload
 */
function triggerFileUpload() {
	fileInputRef.value?.click();
}

/**
 * Handles file selection from the file input
 */
async function handleFileSelect(event: Event) {
	const input = event.target as HTMLInputElement;
	const file = input.files?.[0];

	if (!file) {
		return;
	}

	pendingFile.value = file;

	if (resume.value) {
		showUploadConfirmation.value = true;
	} else {
		await processUpload(file);
		pendingFile.value = null;
	}

	// Reset input so the same file can be selected again
	input.value = "";
}

/**
 * Confirms upload and processes the file
 */
async function confirmUpload() {
	showUploadConfirmation.value = false;

	if (pendingFile.value) {
		await processUpload(pendingFile.value);
		pendingFile.value = null;
	} else {
		// User confirmed but we don't have a pending file, trigger input
		fileInputRef.value?.click();
	}
}

function cancelUpload() {
	showUploadConfirmation.value = false;
	pendingFile.value = null;
}

/**
 * Processes the uploaded JSON Resume file
 */
async function processUpload(file: File) {
	try {
		const result = await importJson(file);

		if (result.success && result.data) {
			setResume(result.data);
			toast({
				title: "Import successful",
				description: "Your resume has been loaded successfully.",
				variant: "default",
			});
		} else {
			// Show validation errors
			showValidationPanel.value = true;
			toast({
				title: "Import failed",
				description: `Found ${result.errors?.length || 0} validation errors.`,
				variant: "destructive",
			});
		}
	} catch (error) {
		toast({
			title: "Import error",
			description:
				error instanceof Error ? error.message : "Failed to import resume",
			variant: "destructive",
		});
	}
}

/**
 * Exports the current resume as JSON
 */
function handleExportJson() {
	if (!resume.value) {
		toast({
			title: "No data to export",
			description: "Please fill in some resume information first.",
			variant: "destructive",
		});
		return;
	}

	const success = exportJson(resume.value, "resume.json");

	if (success) {
		toast({
			title: "Export successful",
			description: "Your resume has been downloaded as resume.json",
			variant: "default",
		});
	} else {
		showValidationPanel.value = true;
		toast({
			title: "Export failed",
			description: "Please fix validation errors before exporting.",
			variant: "destructive",
		});
	}
}

/**
 * Validates the current resume and shows the validation panel
 */
function handleValidateJson() {
	if (!resume.value) {
		toast({
			title: "No data to validate",
			description: "Please fill in some resume information first.",
			variant: "destructive",
		});
		return;
	}

	validateResume(resume.value);
	showValidationPanel.value = true;
}

/**
 * Handles jump-to navigation from validation errors
 */
function handleJumpTo(section: string, path: string) {
	// TODO: Implement scroll and highlight logic when validation errors provide detailed paths
	console.log("Jump to:", section, path);
	showValidationPanel.value = false;
}

/**
 * Shows reset confirmation dialog
 */
function handleResetForm() {
	if (!resume.value) {
		toast({
			title: "No data to reset",
			description: "The form is already empty.",
			variant: "default",
		});
		return;
	}
	showResetConfirmation.value = true;
}

/**
 * Confirms and executes form reset
 */
async function confirmReset() {
	showResetConfirmation.value = false;

	try {
		// Clear the form
		clearForm();

		// Clear storage
		await resumeStore.clearStorage();

		hasUnsavedChanges.value = false;

		toast({
			title: "Form reset",
			description: "All resume data has been cleared.",
			variant: "default",
		});
	} catch (error) {
		toast({
			title: "Reset failed",
			description:
				error instanceof Error ? error.message : "Failed to reset form",
			variant: "destructive",
		});
	}
}

function cancelReset() {
	showResetConfirmation.value = false;
}

// Ref to ResumeForm instance
const resumeFormRef = ref();

/**
 * Handles navigation from preview to form section or entry
 * @param section - section name
 * @param entryIndex - optional array entry index
 */
function handlePreviewNavigate(section: string, entryIndex?: number) {
	nextTick(() => {
		const formEl = resumeFormRef.value?.$el;
		if (!formEl) return;
		const sectionMap: Record<string, string> = {
			basics: "section-basics",
			work: "section-work",
			education: "section-education",
			skills: "section-skills",
			projects: "section-projects",
			languages: "section-languages",
			volunteer: "section-volunteer",
			certificates: "section-certificates",
			awards: "section-awards",
			publications: "section-publications",
			interests: "section-interests",
			references: "section-references",
		};
		const refName = sectionMap[section];
		if (!refName) return;
		let target: HTMLElement | null = null;
		// If entryIndex is provided, look for array entry
		if (typeof entryIndex === "number") {
			// Try to find entry by data-entry-id
			target = formEl.querySelector(
				`[ref='${refName}'] [data-entry-id='${entryIndex}']`,
			);
		}
		// Fallback to section root
		if (!target) {
			target = formEl.querySelector(`[ref='${refName}']`);
		}
		if (target) {
			target.scrollIntoView({ behavior: "smooth", block: "start" });
			target.classList.add("section-highlight");
			// Move focus to first input in section/entry
			const input = target.querySelector("input, textarea, select, [tabindex]");
			if (input) {
				(input as HTMLElement).focus();
			}
			setTimeout(() => target.classList.remove("section-highlight"), 2000);
		}
	});
}
</script>

<template>
  <DashboardLayout>
    <div class="container mx-auto py-8 px-4">
      <!-- Header with Action Buttons -->
      <div class="mb-4 flex flex-col gap-4 md:flex-row md:justify-between md:items-center">
        <div>
          <h1 class="text-3xl font-bold text-foreground">
            {{ t("resume.title") }}
          </h1>
          <p class="text-muted-foreground">
            {{ t("resume.subtitle") }}
          </p>
        </div>

        <!-- Utility Bar -->
        <div class="flex flex-wrap gap-2">
          <!-- Last saved indicator -->
          <div v-if="lastSavedText" class="flex items-center text-sm text-muted-foreground px-3">
            {{ lastSavedText }}
          </div>

          <Button
              variant="outline"
              size="sm"
              @click="triggerFileUpload"
              :title="t('resume.buttons.uploadJsonHint')"
          >
            <Upload class="h-4 w-4 mr-2" />
            {{ t('resume.buttons.uploadJson') }}
          </Button>

          <Button
              variant="outline"
              size="sm"
              @click="handleExportJson"
              :title="t('resume.buttons.downloadJsonHint')"
          >
            <Download class="h-4 w-4 mr-2" />
            {{ t('resume.buttons.downloadJson') }}
          </Button>

          <Button
              variant="outline"
              size="sm"
              @click="handleValidateJson"
              :title="t('resume.buttons.validateJsonHint')"
          >
            <CheckCircle class="h-4 w-4 mr-2" />
            {{ t('resume.buttons.validateJson') }}
          </Button>

          <Button
              size="sm"
              :title="t('resume.buttons.resetFormHint')"
              variant="outline"
              @click="handleResetForm"
          >
            <RotateCcw class="h-4 w-4 mr-2"/>
            {{ t('resume.buttons.resetForm') }}
          </Button>

          <Button
              size="sm"
              :title="t('resume.buttons.generatePdfHint')"
              variant="default"
              @click="router.push('/resume/pdf')"
          >
            <FileText class="h-4 w-4 mr-2"/>
            {{ t('resume.buttons.generatePdf') }}
          </Button>

          <Button
              variant="outline"
              @click="togglePreview"
              class="hidden lg:flex"
              :title="showPreview ? t('resume.preview.hide') : t('resume.preview.show')"
          >
            <svg
                class="h-4 w-4 mr-2"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
            >
              <path
                  v-if="showPreview"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
              />
              <g v-else>
                <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                />
                <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                />
              </g>
            </svg>
            {{ showPreview ? t('resume.preview.hide') : t('resume.buttons.preview') }}
          </Button>
        </div>
      </div>

      <!-- Hidden file input for JSON upload -->
      <input
          ref="fileInputRef"
          type="file"
          accept=".json,application/json"
          class="hidden"
          @change="handleFileSelect"
      />

      <div class="grid gap-6" :class="showPreview ? 'lg:grid-cols-2' : 'lg:grid-cols-1'">
        <!-- Form Section -->
        <Card class="h-fit">
          <CardHeader>
            <CardTitle>
              {{ t("resume.sections.personalInfo") }}
            </CardTitle>
            <CardDescription>
              Fill out the form to create your resume
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResumeForm ref="resumeFormRef"/>
          </CardContent>
        </Card>

        <!-- Preview Section -->
        <Card v-if="showPreview"
              class="hidden lg:flex lg:flex-col sticky top-8 max-h-[calc(100vh-6rem)] overflow-hidden">
          <CardHeader class="flex-none">
            <CardTitle>
              {{ t('resume.buttons.preview') }}
            </CardTitle>
            <CardDescription>
              Live preview of your resume
            </CardDescription>
          </CardHeader>
          <CardContent class="flex-1 p-0 overflow-hidden">
            <div class="h-full overflow-y-auto">
              <ResumePreview :data="resume" @navigate-section="handlePreviewNavigate"/>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>

    <!-- Upload Confirmation Dialog -->
    <AlertDialog v-model:open="showUploadConfirmation">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Replace existing data?</AlertDialogTitle>
          <AlertDialogDescription>
            You have unsaved changes in your resume. Uploading a new JSON file will replace all current data. This action cannot be undone.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="cancelUpload">Cancel</AlertDialogCancel>
          <AlertDialogAction @click="confirmUpload">
            Continue
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <!-- Reset Confirmation Dialog -->
    <AlertDialog v-model:open="showResetConfirmation">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{{ t('resume.resetDialog.title') }}</AlertDialogTitle>
          <AlertDialogDescription>
            {{ t('resume.resetDialog.description') }}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="cancelReset">{{
              t('resume.resetDialog.cancel')
            }}
          </AlertDialogCancel>
          <AlertDialogAction class="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                             @click="confirmReset">
            {{ t('resume.resetDialog.reset') }}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <!-- Validation Error Panel -->
    <ValidationErrorPanel
        v-model:open="showValidationPanel"
        :errors="validationErrors"
        @jump-to="handleJumpTo"
    />
  </DashboardLayout>
</template>

<style scoped>
.section-highlight {
  box-shadow: 0 0 0 3px var(--accent);
  transition: box-shadow 0.3s;
}
</style>
