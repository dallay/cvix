<script setup lang="ts">
import { ChevronRight } from "lucide-vue-next";
import type { AccordionTriggerProps } from "reka-ui";
import { AccordionHeader, AccordionTrigger } from "reka-ui";
import { cn } from "@/lib/utils";

const props = defineProps<AccordionTriggerProps & { class?: string }>();
</script>

<template>
  <AccordionHeader class="flex">
    <AccordionTrigger
      v-bind="props"
      :class="
        cn(
          'flex flex-1 items-center justify-between py-4 text-sm font-medium transition-all hover:underline [&[data-state=open]>svg]:rotate-90',
          props.class,
        )
      "
      data-slot="accordion-trigger"
    >
      <slot />
      <ChevronRight
        class="h-4 w-4 shrink-0 text-muted-foreground transition-transform duration-200"
      />
    </AccordionTrigger>
  </AccordionHeader>
</template>
