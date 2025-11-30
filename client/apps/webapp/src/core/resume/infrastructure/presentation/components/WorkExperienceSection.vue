<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldDescription,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import DatePicker from "@/components/ui/date-picker/DatePicker.vue";
import type { Work } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const workExperiences = defineModel<Work[]>({
	default: () => [],
});

const addWorkExperience = () => {
	workExperiences.value.push({
		name: "",
		position: "",
		url: "",
		startDate: "",
		endDate: "",
		summary: "",
		highlights: [],
	});
};

const removeWorkExperience = (index: number) => {
	workExperiences.value.splice(index, 1);
};

const addHighlight = (workIndex: number) => {
	const work = workExperiences.value[workIndex];
	if (work) {
		work.highlights = [...work.highlights, ""];
	}
};

const removeHighlight = (workIndex: number, highlightIndex: number) => {
	const work = workExperiences.value[workIndex];
	if (work) {
		work.highlights = work.highlights.filter((_, i) => i !== highlightIndex);
	}
};

const updateHighlight = (
	workIndex: number,
	highlightIndex: number,
	value: string | number,
) => {
	const work = workExperiences.value[workIndex];
	if (work && typeof value === "string") {
		const newHighlights = [...work.highlights];
		newHighlights[highlightIndex] = value;
		work.highlights = newHighlights;
	}
};

const setEndDate = (index: number, date: string) => {
	if (workExperiences.value[index]) {
		workExperiences.value[index].endDate = date;
	}
};

const hasWorkExperiences = computed(() => workExperiences.value.length > 0);
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <FieldDescription>
        {{ t('resume.actions.descriptions.workExperience') }}
      </FieldDescription>
      <Button
        type="button"
        variant="outline"
        size="sm"
        @click="addWorkExperience"
        data-testid="add-work-experience"
      >
        <Plus class="h-4 w-4 mr-2" />
        {{ t('resume.actions.addWorkExperience') }}
      </Button>
    </div>

    <div v-if="hasWorkExperiences" class="space-y-4">
      <div
        v-for="(work, workIndex) in workExperiences"
        :key="workIndex"
        class="border border-border rounded-lg p-4 space-y-4 bg-card"
      >
        <div class="flex items-center justify-between">
          <h4 class="text-sm font-medium">
            {{ t('resume.actions.labels.workExperience', { number: workIndex + 1 }) }}
          </h4>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            @click="removeWorkExperience(workIndex)"
          >
            <Trash2 class="h-4 w-4" />
          </Button>
        </div>

        <FieldGroup>
          <Field>
            <FieldLabel :for="`work-name-${workIndex}`">
              {{ t('resume.fields.company') }}
            </FieldLabel>
            <Input
              :id="`work-name-${workIndex}`"
              v-model="work.name"
              type="text"
              :placeholder="t('resume.placeholders.company')"
              :data-testid="`work-name-${workIndex}`"
              required
            />
          </Field>

          <Field>
            <FieldLabel :for="`work-position-${workIndex}`">
              {{ t('resume.fields.position') }}
            </FieldLabel>
            <Input
              :id="`work-position-${workIndex}`"
              v-model="work.position"
              type="text"
              :placeholder="t('resume.placeholders.position')"
              :data-testid="`work-position-${workIndex}`"
            />
          </Field>

          <Field>
            <FieldLabel :for="`work-start-date-${workIndex}`">
              {{ t('resume.fields.startDate') }}
            </FieldLabel>
            <DatePicker
              :id="`work-start-date-${workIndex}`"
              v-model="work.startDate"
              :data-testid="`work-start-date-${workIndex}`"
            />
          </Field>

          <Field>
            <FieldLabel :for="`work-end-date-${workIndex}`">
              {{ t('resume.fields.endDate') }}
            </FieldLabel>
            <DatePicker
              :id="`work-end-date-${workIndex}`"
              v-model="work.endDate"
              :data-testid="`work-end-date-${workIndex}`"
            />
          </Field>
        </FieldGroup>

        <div>
          <h5 class="text-sm font-semibold mb-2">
            {{ t('resume.fields.highlights') }}
          </h5>
          <div
            v-for="(highlight, highlightIndex) in work.highlights"
            :key="highlightIndex"
            class="flex items-center mb-2"
          >
            <Input
              :id="`work-highlight-${workIndex}-${highlightIndex}`"
              :model-value="highlight"
              type="text"
              :placeholder="t('resume.placeholders.highlight')"
              :data-testid="`work-highlight-${workIndex}-${highlightIndex}`"
              @update:model-value="updateHighlight(workIndex, highlightIndex, $event)"
            />
            <Button
              type="button"
              variant="ghost"
              size="sm"
              @click="removeHighlight(workIndex, highlightIndex)"
            >
              <Trash2 class="h-4 w-4" />
            </Button>
          </div>
          <Button
            type="button"
            variant="outline"
            size="sm"
            @click="addHighlight(workIndex)"
          >
            <Plus class="h-4 w-4 mr-2" />
            {{ t('resume.actions.addHighlight') }}
          </Button>
        </div>
      </div>
    </div>

    <div
      v-else
      class="text-center py-8 border border-dashed border-border rounded-lg bg-muted/50"
    >
      <p class="text-sm text-muted-foreground">
        {{ t('resume.actions.empty.workExperience') }}
      </p>
      <Button
        type="button"
        variant="link"
        size="sm"
        class="mt-2"
        @click="addWorkExperience"
      >
        {{ t('resume.actions.addFirstWorkExperience') }}
      </Button>
    </div>
  </div>
</template>

<style scoped>

</style>
