<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { useResumeGeneration } from "@/resume/composables/useResumeGeneration";
import { useResumeStore } from "@/resume/stores/resumeStore";
import EducationSection from "./EducationSection.vue";
import PersonalInfoSection from "./PersonalInfoSection.vue";
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

const contentError = computed(() =>
	store.hasContent ? null : t("resume.validation.content_required"),
);

async function handleSubmit() {
	if (!store.isValid) {
		return;
	}

	await generateResume(store.resume, locale.value);
}
</script>

<template>
  <form data-testid="resume-form" class="space-y-8" @submit.prevent="handleSubmit">
    <Alert v-if="!store.hasContent" data-testid="content-error" variant="destructive">
      <AlertDescription>{{ contentError }}</AlertDescription>
    </Alert>

    <Alert v-if="generationError" data-testid="generation-error" variant="destructive">
      <AlertDescription>{{ generationError.message }}</AlertDescription>
    </Alert>

    <div v-if="isGenerating" data-testid="loading-indicator" class="flex items-center gap-2">
      <div class="animate-spin rounded-full h-4 w-4 border-b-2 border-primary"></div>
      <span>{{ t('resume.loading.generating') }}</span>
      <span class="text-muted-foreground">{{ progress }}%</span>
    </div>

    <PersonalInfoSection />
    <WorkExperienceSection />
    <EducationSection />
    <SkillsSection />

    <div class="flex justify-end">
      <Button
        data-testid="submit-button"
        type="submit"
        :disabled="isGenerating"
        size="lg"
      >
        {{ t('resume.form.generate') }}
      </Button>
    </div>
  </form>
</template>
