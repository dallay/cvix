<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Education } from "@/core/resume/domain/Resume";

interface Props {
	education: Education;
}

const props = defineProps<Props>();
const { t } = useI18n();

/**
 * Formats date range for display
 */
function formatDateRange(startDate: string, endDate: string): string {
	const end = endDate || t("resume.preview.present");
	return `${startDate} - ${end}`;
}
</script>

<template>
	<article class="space-y-2">
    <div class="flex justify-between items-start gap-4">
      <div class="flex-1">
        <h3 class="font-semibold text-foreground">{{ education.studyType }} {{ education.area }}</h3>
        <p class="text-sm text-muted-foreground">{{ education.institution }}</p>
      </div>
      <time class="text-xs text-muted-foreground whitespace-nowrap">
        {{ formatDateRange(education.startDate, education.endDate) }}
      </time>
    </div>

    <p v-if="education.score" class="text-sm text-foreground">
      <span class="font-medium">{{ t('resume.preview.score') }}:</span> {{ education.score }}
    </p>

    <div v-if="education.courses?.length" class="text-sm">
      <p class="font-medium text-foreground mb-1">{{ t('resume.preview.courses') }}:</p>
      <ul class="list-disc list-inside space-y-0.5 text-foreground">
        <li v-for="(course, index) in education.courses" :key="index">{{ course }}</li>
      </ul>
    </div>
  </article>
</template>
