<script lang="ts" setup>
import { reactiveOmit } from "@vueuse/core";
import { ChevronRight } from "lucide-vue-next";
import type { RangeCalendarNextProps } from "reka-ui";
import { RangeCalendarNext, useForwardProps } from "reka-ui";
import type { HTMLAttributes } from "vue";
import { buttonVariants } from "../button/index.ts";
import { cn } from "../../../lib/utils.ts";

const props = defineProps<
	RangeCalendarNextProps & { class?: HTMLAttributes["class"] }
>();

const delegatedProps = reactiveOmit(props, "class");

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <RangeCalendarNext
    data-slot="range-calendar-next-button"
    :class="cn(
      buttonVariants({ variant: 'outline' }),
      'absolute right-1',
      'size-7 bg-transparent p-0 opacity-50 hover:opacity-100',
      props.class,
    )"
    v-bind="forwardedProps"
  >
    <slot>
      <ChevronRight class="size-4" />
    </slot>
  </RangeCalendarNext>
</template>
