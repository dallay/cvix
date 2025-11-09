<script setup lang="ts">
import type { Skill } from "@/core/resume/domain/Resume";

interface Props {
	skills: ReadonlyArray<Skill>;
}

const props = defineProps<Props>();
</script>

<template>
  <div class="space-y-3">
    <div
        v-for="(skill, index) in skills"
        :key="index"
        class="space-y-1"
    >
      <div class="flex items-baseline justify-between gap-2">
        <h4 class="font-medium text-foreground">{{ skill.name }}</h4>
        <span v-if="skill.level" class="text-xs text-muted-foreground">{{ skill.level }}</span>
      </div>
      <div v-if="skill.keywords?.length" class="flex flex-wrap gap-2">
        <span
            v-for="(keyword, kIndex) in skill.keywords"
            :key="kIndex"
            class="inline-block px-2 py-1 text-xs bg-muted text-muted-foreground rounded"
        >
          {{ keyword }}
        </span>
      </div>
    </div>
  </div>
</template>
