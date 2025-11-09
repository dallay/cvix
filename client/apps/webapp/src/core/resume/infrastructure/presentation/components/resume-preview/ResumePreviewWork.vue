<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Work } from "@/core/resume/domain/Resume";

interface Props {
	work: Work;
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
        <h3 class="font-semibold text-foreground">{{ work.position }}</h3>
        <p class="text-sm text-muted-foreground">
          {{ work.name }}
          <span v-if="work.url"> • <a :href="work.url" target="_blank" rel="noopener noreferrer"
                                       class="hover:underline">Website</a></span>
        </p>
      </div>
      <time class="text-xs text-muted-foreground whitespace-nowrap">
        {{ formatDateRange(work.startDate, work.endDate) }}
      </time>
    </div>

    <p v-if="work.summary" class="text-sm text-foreground leading-relaxed">
      {{ work.summary }}
    </p>

    <ul v-if="work.highlights?.length" class="list-disc list-inside space-y-1 text-sm text-foreground">
      <li v-for="(highlight, index) in work.highlights" :key="index" class="leading-relaxed">
        {{ highlight }}
      </li>
    </ul>
  </article>
</template>
