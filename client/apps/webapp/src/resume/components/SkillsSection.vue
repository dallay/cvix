<script setup lang="ts">
import { Plus, Trash2, X } from "lucide-vue-next";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useResumeStore } from "@/resume/stores/resumeStore";

const { t } = useI18n();
const store = useResumeStore();

const newKeywords = ref<Record<number, string>>({});

function updateCategoryName(index: number, name: string) {
	if (store.resume.skills?.[index]) {
		const updated = { ...store.resume.skills[index], name };
		store.updateSkillCategory(index, updated);
	}
}

function addKeyword(index: number) {
	const keyword = newKeywords.value[index]?.trim();
	if (!keyword || !store.resume.skills || !store.resume.skills[index]) return;

	const category = store.resume.skills[index];
	if (category.keywords.includes(keyword)) return;

	const updated = { ...category, keywords: [...category.keywords, keyword] };
	store.updateSkillCategory(index, updated);
	newKeywords.value[index] = "";
}

function removeKeyword(categoryIndex: number, keywordIndex: number) {
	if (store.resume.skills?.[categoryIndex]) {
		const category = store.resume.skills[categoryIndex];
		const keywords = [...category.keywords];
		keywords.splice(keywordIndex, 1);
		const updated = { ...category, keywords };
		store.updateSkillCategory(categoryIndex, updated);
	}
}
</script>

<template>
  <section data-testid="skills-section" class="space-y-6">
    <div class="flex items-center justify-between">
      <h2 class="text-2xl font-semibold">{{ t('resume.sections.skills') }}</h2>
      <Button data-testid="add-skill-category" type="button" variant="outline" size="sm" @click="store.addSkillCategory()">
        <Plus class="h-4 w-4 mr-2" />
  {{ t('resume.buttons.add') }}
      </Button>
    </div>

    <p v-if="!store.resume.skills || store.resume.skills.length === 0" class="text-muted-foreground text-center py-8">
      {{ t('resume.sections.noEntries') }}
    </p>

    <div v-for="(category, index) in store.resume.skills" :key="index" data-testid="skill-category" class="space-y-4 p-4 border rounded-lg relative">
      <Button
        :data-testid="`remove-skill-category-${index}`"
        type="button"
        variant="ghost"
        size="sm"
        class="absolute top-2 right-2"
        @click="store.removeSkillCategory(index)"
      >
        <Trash2 class="h-4 w-4" />
      </Button>

      <div>
        <Label :for="`category-name-${index}`">{{ t('resume.fields.categoryName') }} <span class="text-destructive">*</span></Label>
        <Input
          :id="`category-name-${index}`"
          :data-testid="`category-name-input-${index}`"
          type="text"
          :value="category.name"
          maxlength="100"
          @input="updateCategoryName(index, ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div>
        <Label>{{ t('resume.fields.skills') }}</Label>
        <div class="flex flex-wrap gap-2 mb-2">
          <Badge
            v-for="(keyword, keywordIndex) in category.keywords"
            :key="keywordIndex"
            variant="secondary"
            class="flex items-center gap-1"
          >
            {{ keyword }}
            <button
              :data-testid="`remove-keyword-${index}-${keywordIndex}`"
              type="button"
              class="hover:bg-destructive/10 rounded-full p-0.5"
              @click="removeKeyword(index, keywordIndex)"
            >
              <X class="h-3 w-3" />
            </button>
          </Badge>
        </div>
        <div class="flex gap-2">
          <Input
            :data-testid="`keyword-input-${index}`"
            type="text"
            v-model="newKeywords[index]"
            maxlength="50"
            :placeholder="t('resume.placeholders.addSkill')"
            @keyup.enter="addKeyword(index)"
          />
          <Button type="button" variant="outline" size="sm" @click="addKeyword(index)">
            <Plus class="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  </section>
</template>
