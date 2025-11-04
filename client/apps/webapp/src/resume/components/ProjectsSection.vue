<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useResumeStore } from "@/resume/stores/resumeStore";
import type { Project } from "@/resume/types/resume";

const { t } = useI18n();
const store = useResumeStore();

function updateEntry(index: number, field: keyof Project, value: string) {
	if (store.resume.projects?.[index]) {
		const updated = { ...store.resume.projects[index], [field]: value };
		store.updateProject(index, updated);
	}
}
</script>

<template>
  <section data-testid="projects-section" aria-labelledby="projects-heading" class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 id="projects-heading" class="text-2xl font-semibold">{{ t('resume.sections.projects') }}</h2>
      <Button data-testid="add-project" type="button" variant="outline" size="sm" @click="store.addProject()">
        <Plus class="h-4 w-4 mr-2" />
        {{ t('resume.actions.add') }}
      </Button>
    </div>

    <p v-if="!store.resume.projects || store.resume.projects.length === 0" class="text-muted-foreground text-center py-8">
      {{ t('resume.sections.noEntries') }}
    </p>

    <div v-for="(entry, index) in store.resume.projects" :key="index" data-testid="project-entry" class="space-y-4 p-4 border rounded-lg relative">
      <Button
        :data-testid="`remove-project-${index}`"
        type="button"
        variant="ghost"
        size="sm"
        class="absolute top-2 right-2"
        :aria-label="t('resume.actions.removeProject')"
        @click="store.removeProject(index)"
      >
        <Trash2 class="h-4 w-4" />
      </Button>

      <div>
        <Label :for="`project-name-${index}`">{{ t('resume.fields.projectName') }} <span class="text-destructive">*</span></Label>
        <Input
          :id="`project-name-${index}`"
          :data-testid="`project-name-input-${index}`"
          type="text"
          :value="entry.name"
          maxlength="100"
          @input="updateEntry(index, 'name', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div>
        <Label :for="`project-description-${index}`">{{ t('resume.fields.description') }} <span class="text-destructive">*</span></Label>
        <Textarea
          :id="`project-description-${index}`"
          :data-testid="`project-description-input-${index}`"
          :value="entry.description"
          maxlength="500"
          @input="updateEntry(index, 'description', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div>
        <Label :for="`project-url-${index}`">{{ t('resume.fields.url') }}</Label>
        <Input
          :id="`project-url-${index}`"
          :data-testid="`project-url-input-${index}`"
          type="url"
          :value="entry.url"
          maxlength="200"
          @input="updateEntry(index, 'url', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`project-start-date-${index}`">{{ t('resume.fields.startDate') }}</Label>
          <Input
            :id="`project-start-date-${index}`"
            :data-testid="`project-start-date-input-${index}`"
            type="date"
            :value="entry.startDate"
            @input="updateEntry(index, 'startDate', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`project-end-date-${index}`">{{ t('resume.fields.endDate') }}</Label>
          <Input
            :id="`project-end-date-${index}`"
            :data-testid="`project-end-date-input-${index}`"
            type="date"
            :value="entry.endDate"
            @input="updateEntry(index, 'endDate', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>
    </div>
  </section>
</template>
