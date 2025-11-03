<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

import type { Project } from "../types/resume";

const props = defineProps<{
	projects: Project[];
}>();

const emit = defineEmits<{
	"update:projects": [projects: Project[]];
}>();

const { t } = useI18n();

const localProjects = computed({
	get: () => props.projects,
	set: (value) => emit("update:projects", value),
});

const addProject = () => {
	localProjects.value = [
		...localProjects.value,
		{
			name: "",
			description: "",
			url: "",
			startDate: "",
			endDate: "",
		},
	];
};

const removeProject = (index: number) => {
	localProjects.value = localProjects.value.filter((_, i) => i !== index);
};

const updateProject = (
	index: number,
	field: keyof Project,
	value: string | number,
) => {
	const updated = [...localProjects.value];
	const stringValue = String(value);
	updated[index] = { ...updated[index], [field]: stringValue } as Project;
	localProjects.value = updated;
};
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h3 class="text-lg font-semibold">
        {{ t("resume.sections.projects") }}
      </h3>
      <Button
        type="button"
        variant="outline"
        size="sm"
        @click="addProject"
        data-testid="add-project"
      >
        {{ t("resume.buttons.addProject") }}
      </Button>
    </div>

    <div
      v-if="localProjects.length === 0"
      class="text-sm text-muted-foreground"
    >
      {{ t("resume.sections.noEntries") }}
    </div>

    <div
      v-for="(project, index) in localProjects"
      :key="index"
      class="space-y-4"
    >
      <Card data-testid="project-entry">
        <CardContent class="pt-6">
          <div class="grid gap-4">
            <div class="grid gap-2">
              <Label :for="`project-name-${index}`">
                {{ t("resume.fields.projectName") }} *
              </Label>
              <Input
                :id="`project-name-${index}`"
                :model-value="project.name"
                :placeholder="t('resume.placeholders.projectName')"
                @update:model-value="(value) => updateProject(index, 'name', String(value))"
                data-testid="project-name-input"
              />
            </div>

            <div class="grid gap-2">
              <Label :for="`project-description-${index}`">
                {{ t("resume.fields.description") }} *
              </Label>
              <Textarea
                :id="`project-description-${index}`"
                :model-value="project.description"
                :placeholder="t('resume.placeholders.description')"
                @update:model-value="(value) => updateProject(index, 'description', String(value))"
                data-testid="project-description-input"
              />
            </div>

            <div class="grid gap-2">
              <Label :for="`project-url-${index}`">
                {{ t("resume.fields.url") }}
              </Label>
              <Input
                :id="`project-url-${index}`"
                :model-value="project.url"
                :placeholder="t('resume.placeholders.url')"
                @update:model-value="(value) => updateProject(index, 'url', String(value))"
                data-testid="project-url-input"
              />
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div class="grid gap-2">
                <Label :for="`project-start-date-${index}`">
                  {{ t("resume.fields.startDate") }}
                </Label>
                <Input
                  :id="`project-start-date-${index}`"
                  type="date"
                  :model-value="project.startDate"
                  @update:model-value="(value) => updateProject(index, 'startDate', String(value))"
                  data-testid="project-start-date-input"
                />
              </div>

              <div class="grid gap-2">
                <Label :for="`project-end-date-${index}`">
                  {{ t("resume.fields.endDate") }}
                </Label>
                <Input
                  :id="`project-end-date-${index}`"
                  type="date"
                  :model-value="project.endDate"
                  @update:model-value="(value) => updateProject(index, 'endDate', String(value))"
                  data-testid="project-end-date-input"
                />
              </div>
            </div>

            <div class="flex justify-end">
              <Button
                type="button"
                variant="destructive"
                size="sm"
                @click="removeProject(index)"
                data-testid="remove-project"
              >
                {{ t("resume.buttons.remove") }}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>
