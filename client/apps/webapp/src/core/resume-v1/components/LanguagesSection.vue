<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useResumeStore } from "@/core/resume-v1/stores/resumeStore.ts";
import type { Language } from "@/core/resume-v1/types/resume.ts";

const { t } = useI18n();
const store = useResumeStore();

function updateEntry(index: number, field: keyof Language, value: string) {
	if (store.resume.languages?.[index]) {
		const updated = { ...store.resume.languages[index], [field]: value };
		store.updateLanguage(index, updated);
	}
}
</script>

<template>
  <section data-testid="languages-section" aria-labelledby="languages-heading" class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 id="languages-heading" class="text-2xl font-semibold">{{ t('resume.sections.languages') }}</h2>
      <Button data-testid="add-language" type="button" variant="outline" size="sm" @click="store.addLanguage()">
        <Plus class="h-4 w-4 mr-2" />
        {{ t('resume.actions.add') }}
      </Button>
    </div>

    <p v-if="!store.resume.languages || store.resume.languages.length === 0" class="text-muted-foreground text-center py-8">
      {{ t('resume.sections.noEntries') }}
    </p>

    <div v-for="(entry, index) in store.resume.languages" :key="index" data-testid="language-entry" class="space-y-4 p-4 border rounded-lg relative">
      <Button
        :data-testid="`remove-language-${index}`"
        type="button"
        variant="ghost"
        size="sm"
        class="absolute top-2 right-2"
        :aria-label="t('resume.actions.removeLanguage')"
        @click="store.removeLanguage(index)"
      >
        <Trash2 class="h-4 w-4" />
      </Button>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <Label :for="`language-${index}`">{{ t('resume.fields.language') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`language-${index}`"
            :data-testid="`language-input-${index}`"
            type="text"
            :value="entry.language"
            maxlength="100"
            @input="updateEntry(index, 'language', ($event.target as HTMLInputElement).value)"
          />
        </div>

        <div>
          <Label :for="`fluency-${index}`">{{ t('resume.fields.fluency') }} <span class="text-destructive">*</span></Label>
          <Input
            :id="`fluency-${index}`"
            :data-testid="`fluency-input-${index}`"
            type="text"
            :value="entry.fluency"
            maxlength="100"
            @input="updateEntry(index, 'fluency', ($event.target as HTMLInputElement).value)"
          />
        </div>
      </div>
    </div>
  </section>
</template>
