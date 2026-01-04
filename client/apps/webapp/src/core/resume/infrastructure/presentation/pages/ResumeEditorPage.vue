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
	Eye,
	EyeOff,
	FileText,
	Loader2,
	RotateCcw,
	Save,
	Upload,
} from "lucide-vue-next";
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { toast } from "vue-sonner";
import type { Resume } from "@/core/resume/domain/Resume";
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

// Auto-refresh trigger for timestamp (updates every minute)
const timestampRefreshTrigger = ref(0);

// Format last saved timestamp
const lastSavedText = computed(() => {
	// Access the trigger to make this computed reactive to timer updates
	void timestampRefreshTrigger.value;

	if (!resumeStore.lastSavedAt) {
		return null;
	}

	const savedDate = new Date(resumeStore.lastSavedAt);
	const now = new Date();
	const diffMs = now.getTime() - savedDate.getTime();
	const diffMinutes = Math.floor(diffMs / 60000);
	const diffHours = Math.floor(diffMs / 3600000);
	const diffDays = Math.floor(diffMs / 86400000);

	// Use i18n for relative time formatting
	if (diffMinutes < 1) {
		return t("resume.messages.savedJustNow");
	}
	if (diffMinutes < 60) {
		return t("resume.messages.savedMinutesAgo", { count: diffMinutes });
	}
	if (diffHours < 24) {
		return t("resume.messages.savedHoursAgo", { count: diffHours });
	}
	if (diffDays < 7) {
		return t("resume.messages.savedDaysAgo", { count: diffDays });
	}
	// For older dates, show the actual date
	return t("resume.messages.savedOnDate", {
		date: savedDate.toLocaleDateString(),
	});
});

// Warn user about unsaved changes before navigating away
function handleBeforeUnload(event: BeforeUnloadEvent) {
	if (hasUnsavedChanges.value && resume.value) {
		// Modern browsers display a generic message, but we still need to set returnValue
		event.preventDefault();
		event.returnValue = "";
		return "";
	}
}

// Timestamp auto-refresh interval
let timestampInterval: number | null = null;

onMounted(() => {
	if (typeof window !== "undefined") {
		window.addEventListener("beforeunload", handleBeforeUnload);

		// Auto-refresh timestamp display every minute
		timestampInterval = window.setInterval(() => {
			timestampRefreshTrigger.value++;
		}, 60000); // 60 seconds
	}
});

onUnmounted(() => {
	if (typeof window !== "undefined") {
		window.removeEventListener("beforeunload", handleBeforeUnload);

		// Clean up interval timer
		if (timestampInterval !== null) {
			clearInterval(timestampInterval);
			timestampInterval = null;
		}
	}
});

// Keyboard shortcuts with preventDefault to stop browser save dialog
const keys = useMagicKeys({
	passive: false,
	onEventFired(e) {
		// Prevent browser default save dialog for Cmd/Ctrl+S
		if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "s") {
			e.preventDefault();
			e.stopPropagation();
		}
	},
});
const cmdS = keys["Meta+KeyS"];
const ctrlS = keys["Ctrl+KeyS"];

// Handle Cmd/Ctrl+S for save
watch([cmdS, ctrlS], ([cmd, ctrl]) => {
	if (cmd || ctrl) {
		handleSave();
	}
});

const togglePreview = () => {
	showPreview.value = !showPreview.value;
};

/**
 * Saves the current resume to storage
 */
async function handleSave() {
	if (!resume.value) {
		toast({
			title: t("resume.messages.noDataToSave") || "No data to save",
			description:
				t("resume.messages.fillResumeFirst") ||
				"Please fill in some resume information first.",
			variant: "destructive",
		});
		return;
	}

	// Sync the composable state to the store before saving
	resumeStore.setResume(resume.value);

	try {
		await resumeStore.saveToStorage();
		hasUnsavedChanges.value = false;

		toast({
			title: t("resume.messages.saveSuccess") || "Resume saved",
			description:
				t("resume.messages.saveSuccessDescription") ||
				"Your resume has been saved successfully.",
			variant: "default",
		});
	} catch (error) {
		toast({
			title: t("resume.messages.saveError") || "Save failed",
			description:
				error instanceof Error ? error.message : "Failed to save resume",
			variant: "destructive",
		});
	}
}

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
			// Set the composable/store state.
			// useResumeForm() returns a shared reactive instance, so updating it updates any components
			// that bind to its refs via v-model. Use sync helper to centralize optional component calls.
			syncFormWithComposable("load", result.data);

			// Persist to user's configured storage
			resumeStore.setResume(result.data);
			try {
				await resumeStore.saveToStorage();
				hasUnsavedChanges.value = false;
			} catch (storageError) {
				// Log but don't fail the import - data is loaded in memory
				console.warn(
					"[ResumeEditorPage] Failed to persist imported resume to storage:",
					storageError,
				);
			}

			toast({
				title: t("resume.messages.importSuccess"),
				description: t("resume.messages.importSuccessDescription"),
				variant: "default",
			});
		} else {
			// Show validation errors
			showValidationPanel.value = true;
			toast({
				title: t("resume.messages.importFailed"),
				description: t("resume.messages.importFailedDescription", {
					count: result.errors?.length || 0,
				}),
				variant: "destructive",
			});
		}
	} catch (error) {
		toast({
			title: t("resume.messages.importError"),
			description:
				error instanceof Error
					? error.message
					: t("resume.messages.importFailed"),
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
			title: t("resume.messages.noDataToExport"),
			description: t("resume.messages.fillResumeFirst"),
			variant: "destructive",
		});
		return;
	}

	const success = exportJson(resume.value, "resume.json");

	if (success) {
		toast({
			title: t("resume.messages.exportSuccess"),
			description: t("resume.messages.exportSuccessDescription"),
			variant: "default",
		});
	} else {
		showValidationPanel.value = true;
		toast({
			title: t("resume.messages.exportFailed"),
			description: t("resume.messages.exportFailedDescription"),
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
			title: t("resume.messages.noDataToValidate"),
			description: t("resume.messages.fillResumeFirst"),
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
			title: t("resume.messages.noDataToReset"),
			description: t("resume.messages.formAlreadyEmpty"),
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
		syncFormWithComposable("clear");
		// The composable returns a shared reactive instance; clearing it updates bound form fields.
		// Previously we also called `resumeFormRef.value?.clearForm()` — this is redundant and removed.

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
// Note: Only used for DOM navigation in handlePreviewNavigate() to scroll/highlight sections.
// Form state is managed entirely through the shared composable (useResumeForm).
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

// Helper to sync composable state
/**
 * Sync operation between the composable state and the resumeStore.
 * The composable (useResumeForm) is the single source of truth for form state.
 * All updates go through the composable which reactively updates v-model bindings.
 *
 * This helper centralizes all load/clear operations to prevent duplication
 * and ensure consistency across upload, reset, and other workflows.
 */
function syncFormWithComposable(operation: "clear"): void;
function syncFormWithComposable(operation: "load", data: Resume): void;
function syncFormWithComposable(
	operation: "load" | "clear",
	data?: Resume,
): void {
	if (operation === "load") {
		if (!data) {
			throw new Error(
				"syncFormWithComposable('load') requires a Resume argument",
			);
		}
		setResume(data);
	} else if (operation === "clear") {
		clearForm();
	}
}
</script>

<template>
  <DashboardLayout>
    <div class="container mx-auto py-8 px-4">
      <!-- Header with Action Buttons - Sticky -->
      <div class="mb-4 flex flex-col gap-4 md:flex-row md:justify-between md:items-center sticky top-0 z-10 bg-background py-4 -mt-4 border-b border-border">
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
          <!-- Last saved indicator with unsaved changes badge -->
          <div 
            v-if="hasUnsavedChanges || lastSavedText" 
            class="flex items-center text-sm px-3"
            :class="hasUnsavedChanges ? 'text-amber-600 dark:text-amber-400 font-medium' : 'text-muted-foreground'"
          >
            <span v-if="hasUnsavedChanges" class="mr-2">●</span>
            {{ hasUnsavedChanges ? t('resume.messages.unsavedChanges') : lastSavedText }}
          </div>

          <!-- Save Button with unsaved indicator -->
          <Button
              variant="default"
              size="sm"
              @click="handleSave"
              :disabled="resumeStore.isSaving || !resume"
              :title="t('resume.buttons.saveHint') || 'Save resume (Cmd/Ctrl+S)'"
              :class="hasUnsavedChanges ? 'relative' : ''"
          >
            <Loader2 v-if="resumeStore.isSaving" class="h-4 w-4 mr-2 animate-spin" />
            <Save v-else class="h-4 w-4 mr-2" />
            <span class="relative">
              {{ t('resume.buttons.save') || 'Save' }}
              <span 
                v-if="hasUnsavedChanges" 
                class="absolute -top-1 -right-2 h-2 w-2 bg-amber-500 rounded-full"
                aria-label="Unsaved changes indicator"
              />
            </span>
          </Button>

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
              variant="outline"
              @click="router.push('/resume/pdf')"
          >
            <FileText class="h-4 w-4 mr-2"/>
            {{ t('resume.buttons.generatePdf') }}
          </Button>

          <Button
              variant="outline"
              size="sm"
              @click="togglePreview"
              class="hidden lg:flex"
              :title="showPreview ? t('resume.preview.hide') : t('resume.preview.show')"
          >
            <EyeOff v-if="showPreview" class="h-4 w-4 mr-2" />
            <Eye v-else class="h-4 w-4 mr-2" />
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
              {{ t("resume.sections.personalDetails") }}
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
              <ResumePreview v-if="resume" :data="resume" @navigate-section="handlePreviewNavigate"/>
              <div v-else class="flex items-center justify-center h-full text-muted-foreground">
                {{ t('resume.messages.noDataPreview') || 'Add resume data to see preview' }}
              </div>
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
