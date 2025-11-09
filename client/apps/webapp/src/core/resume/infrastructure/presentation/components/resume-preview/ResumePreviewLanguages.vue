<script setup lang="ts">
import type { Language } from "@/core/resume/domain/Resume";

interface Props {
	languages: ReadonlyArray<Language>;
}

const props = defineProps<Props>();
</script>

<template>
  <div class="grid grid-cols-2 gap-3">
    <div
        v-for="(language, index) in languages"
        :key="index"
        class="flex justify-between items-baseline"
    >
      <span class="font-medium text-foreground">{{ language.language }}</span>
      <span class="text-sm text-muted-foreground">{{ language.fluency }}</span>
    </div>
  </div>
</template>
