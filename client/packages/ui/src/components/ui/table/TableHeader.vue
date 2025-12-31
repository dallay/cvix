<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { HTMLAttributes } from "vue";

const props = defineProps<{
	class?: HTMLAttributes["class"];
}>();
</script>

<template>
  <thead
    data-slot="table-header"
    :class="cn('[&_tr]:border-b', props.class)"
  >
    <slot />
  </thead>
</template>
