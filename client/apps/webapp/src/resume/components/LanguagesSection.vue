<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import type { Language } from "../types/resume";

const props = defineProps<{
	languages: Language[];
}>();

const emit = defineEmits<{
	"update:languages": [languages: Language[]];
}>();

const { t } = useI18n();

const localLanguages = computed({
	get: () => props.languages,
	set: (value) => emit("update:languages", value),
});

const addLanguage = () => {
	localLanguages.value = [
		...localLanguages.value,
		{
			language: "",
			fluency: "",
		},
	];
};

const removeLanguage = (index: number) => {
	localLanguages.value = localLanguages.value.filter((_, i) => i !== index);
};

const updateLanguage = (
	index: number,
	field: keyof Language,
	value: string,
) => {
	const updated = [...localLanguages.value];
	updated[index] = { ...updated[index], [field]: value } as Language;
	localLanguages.value = updated;
};
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h3 class="text-lg font-semibold">
        {{ t("resume.sections.languages") }}
      </h3>
      <Button
        type="button"
        variant="outline"
        size="sm"
        @click="addLanguage"
        data-testid="add-language"
      >
        {{ t("resume.buttons.addLanguage") }}
      </Button>
    </div>

    <div
      v-if="localLanguages.length === 0"
      class="text-sm text-muted-foreground"
    >
      {{ t("resume.sections.noEntries") }}
    </div>

    <div
      v-for="(lang, index) in localLanguages"
      :key="index"
      class="space-y-4"
    >
      <Card data-testid="language-entry">
        <CardContent class="pt-6">
          <div class="grid gap-4">
            <div class="grid gap-2">
              <Label :for="`language-${index}`">
                {{ t("resume.fields.language") }}
              </Label>
              <Input
                :id="`language-${index}`"
                :model-value="lang.language"
                :placeholder="t('resume.placeholders.language')"
                @update:model-value="(value: string | number) => updateLanguage(index, 'language', String(value))"
                data-testid="language-input"
              />
            </div>

            <div class="grid gap-2">
              <Label :for="`fluency-${index}`">
                {{ t("resume.fields.fluency") }}
              </Label>
              <Input
                :id="`fluency-${index}`"
                :model-value="lang.fluency"
                :placeholder="t('resume.placeholders.fluency')"
                @update:model-value="(value: string | number) => updateLanguage(index, 'fluency', String(value))"
                data-testid="fluency-input"
              />
            </div>

            <div class="flex justify-end">
              <Button
                type="button"
                variant="destructive"
                size="sm"
                @click="removeLanguage(index)"
                data-testid="remove-language"
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
