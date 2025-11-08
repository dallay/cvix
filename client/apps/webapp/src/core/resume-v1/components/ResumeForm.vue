<script setup lang="ts">
import { Loader2 } from "lucide-vue-next";
import { storeToRefs } from "pinia";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Progress } from "@/components/ui/progress";
import { useResumeGeneration } from "@/core/resume-v1/composables/useResumeGeneration.ts";
import { useResumeSession } from "@/core/resume-v1/composables/useResumeSession.ts";
import { useResumeStore } from "@/core/resume-v1/stores/resumeStore.ts";
import EducationSection from "./EducationSection.vue";
import LanguagesSection from "./LanguagesSection.vue";
import PersonalInfoSection from "./PersonalInfoSection.vue";
import ProjectsSection from "./ProjectsSection.vue";
import SkillsSection from "./SkillsSection.vue";
import WorkExperienceSection from "./WorkExperienceSection.vue";

const { t, locale } = useI18n();
const store = useResumeStore();
const {
	generateResume,
	isGenerating,
	error: generationError,
	progress,
} = useResumeGeneration();

// Session persistence - auto-saves form data
// Use toRef to get reactive ref from store property
const { resume: resumeRef } = storeToRefs(store);
const { clearSession } = useResumeSession(resumeRef);

const contentError = computed(() =>
	store.hasContent ? null : t("resume.validation.content_required"),
);

async function handleSubmit() {
	if (!store.isValid) {
		return;
	}

	const success = await generateResume(store.resume, locale.value);

	// Clear session storage on successful generation
	if (success) {
		clearSession();
	}
}
</script>

<template>
  <form data-testid="resume-form" class="space-y-8" @submit.prevent="handleSubmit">
    <Alert v-if="!store.hasContent" data-testid="content-error" variant="destructive">
      <AlertDescription>{{ contentError }}</AlertDescription>
    </Alert>

    <Alert v-if="generationError" data-testid="generation-error" variant="destructive">
      <AlertDescription>{{ generationError.detail || generationError.title || 'An error occurred' }}</AlertDescription>
    </Alert>

    <!-- Enhanced loading indicator with progress bar -->
    <div
      v-if="isGenerating"
      data-testid="loading-indicator"
      class="rounded-lg border bg-card p-6 space-y-4"
    >
      <div class="flex items-center gap-3">
        <Loader2 class="h-5 w-5 animate-spin text-primary" />
        <div class="flex-1">
          <p class="text-sm font-medium">{{ t('resume.loading.generating') }}</p>
          <p class="text-xs text-muted-foreground">{{ t('resume.loading.please_wait') }}</p>
        </div>
        <span class="text-sm font-semibold text-primary">{{ progress }}%</span>
      </div>
      <Progress :model-value="progress" class="h-2" />
    </div>

    <PersonalInfoSection />
    <WorkExperienceSection />
    <EducationSection />
    <SkillsSection />
    <ProjectsSection />
    <LanguagesSection />

    <div class="flex justify-end">
      <Button
        data-testid="submit-button"
        type="submit"
        :disabled="isGenerating"
        size="lg"
        class="min-w-40"
      >
        <Loader2 v-if="isGenerating" class="mr-2 h-4 w-4 animate-spin" />
        {{ isGenerating ? t('resume.loading.generating') : t('resume.form.generate') }}
      </Button>
    </div>
  </form>
</template>
