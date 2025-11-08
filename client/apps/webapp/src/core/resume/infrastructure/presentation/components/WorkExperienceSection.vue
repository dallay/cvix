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

// Define the Work type
export interface Work {
	company: string;
	position: string;
	startDate: string;
	endDate?: string;
	highlights: string[];
}

const { t } = useI18n();

const workExperiences = defineModel<Work[]>({
	default: () => [],
});

const addWorkExperience = () => {
	workExperiences.value.push({
		company: "",
		position: "",
		startDate: "",
		endDate: "",
		highlights: [],
	});
};

const removeWorkExperience = (index: number) => {
	workExperiences.value.splice(index, 1);
};

const addHighlight = (workIndex: number) => {
	if (workExperiences.value[workIndex]?.highlights) {
		workExperiences.value[workIndex].highlights.push("");
	}
};

const removeHighlight = (workIndex: number, highlightIndex: number) => {
	if (workExperiences.value[workIndex]?.highlights) {
		workExperiences.value[workIndex].highlights.splice(highlightIndex, 1);
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
      <div>
        <FieldLabel>{{ t('resume.sections.workExperience') }}</FieldLabel>
        <FieldDescription>
          {{ t('resume.descriptions.workExperience') }}
        </FieldDescription>
      </div>
      <Button
        type="button"
        variant="outline"
        size="sm"
        @click="addWorkExperience"
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
            {{ t('resume.labels.workExperience', { number: workIndex + 1 }) }}
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
            <FieldLabel :for="`work-company-${workIndex}`">
              {{ t('resume.fields.company') }}
            </FieldLabel>
            <Input
              :id="`work-company-${workIndex}`"
              v-model="work.company"
              type="text"
              :placeholder="t('resume.placeholders.company')"
              :data-testid="`work-company-${workIndex}`"
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
            <Input
              :id="`work-start-date-${workIndex}`"
              v-model="work.startDate"
              type="date"
              :placeholder="t('resume.placeholders.startDate')"
              :data-testid="`work-start-date-${workIndex}`"
            />
          </Field>

          <Field>
            <FieldLabel :for="`work-end-date-${workIndex}`">
              {{ t('resume.fields.endDate') }}
            </FieldLabel>
            <Input
              :id="`work-end-date-${workIndex}`"
              v-model="work.endDate"
              type="date"
              :placeholder="t('resume.placeholders.endDate')"
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
              v-model="work.highlights[highlightIndex]"
              type="text"
              :placeholder="t('resume.placeholders.highlight')"
              :data-testid="`work-highlight-${workIndex}-${highlightIndex}`"
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
        {{ t('resume.empty.workExperience') }}
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
