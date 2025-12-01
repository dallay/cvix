<script setup lang="ts">
import type { AccordionItemProps } from "reka-ui";
import { AccordionItem } from "reka-ui";
import { cn } from "../../../lib/utils.ts";

const props = defineProps<AccordionItemProps & { class?: string }>();
</script>

<template>
  <AccordionItem
    v-bind="props"
    :class="cn('border-b border-border', props.class)"
    data-slot="accordion-item"
  >
    <slot />
  </AccordionItem>
</template>
