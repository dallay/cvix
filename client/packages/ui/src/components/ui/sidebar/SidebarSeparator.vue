<script setup lang="ts">
import type { HTMLAttributes } from "vue";
import { Separator } from "../separator/index.ts";
import { cn } from "../../../lib/utils.ts";

const props = defineProps<{
	class?: HTMLAttributes["class"];
}>();
</script>

<template>
  <Separator
    data-slot="sidebar-separator"
    data-sidebar="separator"
    :class="cn('bg-sidebar-border mx-2 w-auto', props.class)"
  >
    <slot />
  </Separator>
</template>
