<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useResumeStore } from "@/core/resume-v1/stores/resumeStore.ts";
import type { WorkExperience } from "@/core/resume-v1/types/resume.ts";

const { t } = useI18n();
const store = useResumeStore();

function updateEntry(
	index: number,
	field: keyof WorkExperience,
	value: string,
) {
	if (store.resume.work?.[index]) {
		const normalizedValue =
			(field === "startDate" || field === "endDate") && value === ""
				? null
				: value;
		const updated = { ...store.resume.work[index], [field]: normalizedValue };
		store.updateWorkExperience(index, updated);
	}
}
</script>

<template>
  <section data-testid="work-experience-section" aria-labelledby="work-experience-heading" class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 id="work-experience-heading" class="text-2xl font-semibold">{{ t('resume.sections.workExperience') }}</h2>
      <Button data-testid="add-work-experience" type="button" variant="outline" size="sm" @click="store.addWorkExperience()">
        <Plus class="h-4 w-4 mr-2" />
        {{ t('resume.actions.add') }}
      </Button>
    </div>

    <p v-if="!store.resume.work || store.resume.work.length === 0" class="text-muted-foreground text-center py-8">
      {{ t('resume.sections.noEntries') }}
    </p>

    <div v-for="(entry, index) in store.resume.work" :key="index" data-testid="work-experience-entry" class="space-y-4 p-4 border rounded-lg relative">
      <Button
        :data-testid="`remove-work-${index}`"
        type="button"
        variant="ghost"
        size="sm"
        class="absolute top-2 right-2"
        :aria-label="t('resume.actions.removeWorkExperience')"
        @click="store.removeWorkExperience(index)"
      >
        <Trash2 class="h-4 w-4" />
      </Button>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`company-${index}`">{{ t('resume.fields.company') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`company-${index}`"
            :data-testid="`company-input-${index}`"
            type="text"
            :value="entry.company"
            maxlength="100"
            @input="updateEntry(index, 'company', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`position-${index}`">{{ t('resume.fields.position') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`position-${index}`"
            :data-testid="`position-input-${index}`"
            type="text"
            :value="entry.position"
            maxlength="100"
            @input="updateEntry(index, 'position', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`start-date-${index}`">{{ t('resume.fields.startDate') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`start-date-${index}`"
            :data-testid="`start-date-input-${index}`"
            type="date"
            :value="entry.startDate"
            @input="updateEntry(index, 'startDate', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`end-date-${index}`">{{ t('resume.fields.endDate') }}</Label>
          <Input
            :id="`end-date-${index}`"
            :data-testid="`end-date-input-${index}`"
            type="date"
            :value="entry.endDate"
            @input="updateEntry(index, 'endDate', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>

      <div>
        <Label :for="`location-${index}`">{{ t('resume.fields.location') }}</Label>
        <Input
          :id="`location-${index}`"
          :data-testid="`location-input-${index}`"
          type="text"
          :value="entry.location"
          @input="updateEntry(index, 'location', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div>
        <Label :for="`summary-${index}`">{{ t('resume.fields.summary') }}</Label>
        <Textarea
          :id="`summary-${index}`"
          :data-testid="`summary-input-${index}`"
          :value="entry.summary"
          rows="3"
          @input="updateEntry(index, 'summary', ($event.target as HTMLTextAreaElement).value)"
        />
      </div>
    </div>
  </section>
</template>
