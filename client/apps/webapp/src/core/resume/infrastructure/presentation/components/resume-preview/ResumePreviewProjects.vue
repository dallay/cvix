<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { Project } from "@/core/resume/domain/Resume";

interface Props {
	project: Project;
}

const props = defineProps<Props>();
const { t } = useI18n();

/**
 * Formats date range for display
 */
function formatDateRange(startDate: string, endDate: string): string {
	if (!startDate) return "";
	const end = endDate || t("resume.preview.present");
	return `${startDate} - ${end}`;
}
</script>

<template>
	<article class="space-y-2">
    <div class="flex justify-between items-start gap-4">
      <div class="flex-1">
        <h3 class="font-semibold text-foreground">
          {{ project.name }}
          <a
              v-if="project.url"
              :href="project.url"
              target="_blank"
              rel="noopener noreferrer"
              class="ml-2 text-primary hover:underline text-sm"
          >
            <svg class="inline w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
            </svg>
          </a>
        </h3>
      </div>
      <time v-if="project.startDate" class="text-xs text-muted-foreground whitespace-nowrap">
        {{ formatDateRange(project.startDate, project.endDate || '') }}
      </time>
    </div>

    <p v-if="project.description" class="text-sm text-foreground leading-relaxed">
      {{ project.description }}
    </p>

    <div v-if="project.highlights?.length" class="space-y-1">
      <ul class="list-disc list-inside text-sm text-foreground">
        <li v-for="(highlight, index) in project.highlights" :key="index" class="leading-relaxed">
          {{ highlight }}
        </li>
      </ul>
    </div>
  </article>
</template>
