<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { AccordionContentProps } from "reka-ui";
import { AccordionContent } from "reka-ui";

const props = defineProps<AccordionContentProps & { class?: string }>();
</script>

<template>
  <AccordionContent
    :class="
      cn(
        'overflow-hidden text-sm data-[state=closed]:animate-accordion-up data-[state=open]:animate-accordion-down',
        props.class,
      )
    "
    data-slot="accordion-content"
  >
    <div class="pb-4 pt-0">
      <slot />
    </div>
  </AccordionContent>
</template>
