<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { AccordionItemProps } from "reka-ui";
import { AccordionItem } from "reka-ui";

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
