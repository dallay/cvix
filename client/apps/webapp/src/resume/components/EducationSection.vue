<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useResumeStore } from "@/resume/stores/resumeStore";
import type { Education } from "@/resume/types/resume";

const { t } = useI18n();
const store = useResumeStore();

function updateEntry(index: number, field: keyof Education, value: string) {
	if (store.resume.education?.[index]) {
		const updated = { ...store.resume.education[index], [field]: value };
		store.updateEducation(index, updated);
	}
}
</script>

<template>
  <section data-testid="education-section" class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-semibold">{{ t('resume.sections.education') }}</h2>
      <Button data-testid="add-education" type="button" variant="outline" size="sm" @click="store.addEducation()">
        <Plus class="h-4 w-4 mr-2" />
        {{ t('resume.actions.add') }}
      </Button>
    </div>

    <p v-if="!store.resume.education || store.resume.education.length === 0" class="text-muted-foreground text-center py-8">
      {{ t('resume.sections.noEntries') }}
    </p>

    <div v-for="(entry, index) in store.resume.education" :key="index" data-testid="education-entry" class="space-y-4 p-4 border rounded-lg relative">
      <Button
        :data-testid="`remove-education-${index}`"
        type="button"
        variant="ghost"
        size="sm"
        class="absolute top-2 right-2"
        @click="store.removeEducation(index)"
      >
        <Trash2 class="h-4 w-4" />
      </Button>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`institution-${index}`">{{ t('resume.fields.institution') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`institution-${index}`"
            :data-testid="`edu-institution-input-${index}`"
            type="text"
            :value="entry.institution"
            maxlength="100"
            @input="updateEntry(index, 'institution', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`area-${index}`">{{ t('resume.fields.fieldOfStudy') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`area-${index}`"
            :data-testid="`edu-area-input-${index}`"
            type="text"
            :value="entry.area"
            maxlength="100"
            @input="updateEntry(index, 'area', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`study-type-${index}`">{{ t('resume.fields.degree') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`study-type-${index}`"
            :data-testid="`edu-study-type-input-${index}`"
            type="text"
            :value="entry.studyType"
            @input="updateEntry(index, 'studyType', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`score-${index}`">{{ t('resume.fields.gpa') }}</Label>
          <Input
            :id="`score-${index}`"
            :data-testid="`edu-score-input-${index}`"
            type="text"
            :value="entry.score"
            @input="updateEntry(index, 'score', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`start-date-${index}`">{{ t('resume.fields.startDate') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`start-date-${index}`"
            :data-testid="`edu-start-date-input-${index}`"
            type="date"
            :value="entry.startDate"
            @input="updateEntry(index, 'startDate', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`end-date-${index}`">{{ t('resume.fields.endDate') }}</Label>
          <Input
            :id="`end-date-${index}`"
            :data-testid="`edu-end-date-input-${index}`"
            type="date"
            :value="entry.endDate"
            @input="updateEntry(index, 'endDate', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>
    </div>
  </section>
</template>
