<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { SelectLabelProps } from "reka-ui";
import { SelectLabel } from "reka-ui";
import type { HTMLAttributes } from "vue";

const props = defineProps<
	SelectLabelProps & { class?: HTMLAttributes["class"] }
>();
</script>

<template>
  <SelectLabel
    data-slot="select-label"
    :class="cn('px-2 py-1.5 text-sm font-medium', props.class)"
  >
    <slot />
  </SelectLabel>
</template>
