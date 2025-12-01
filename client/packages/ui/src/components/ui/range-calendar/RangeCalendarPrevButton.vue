<script lang="ts" setup>
import { reactiveOmit } from "@vueuse/core";
import { ChevronLeft } from "lucide-vue-next";
import type { RangeCalendarPrevProps } from "reka-ui";
import { RangeCalendarPrev, useForwardProps } from "reka-ui";
import type { HTMLAttributes } from "vue";
import { buttonVariants } from "../button/index.ts";
import { cn } from "../../../lib/utils.ts";

const props = defineProps<
	RangeCalendarPrevProps & { class?: HTMLAttributes["class"] }
>();

const delegatedProps = reactiveOmit(props, "class");

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <RangeCalendarPrev
    data-slot="range-calendar-prev-button"
    :class="cn(
      buttonVariants({ variant: 'outline' }),
      'absolute left-1',
      'size-7 bg-transparent p-0 opacity-50 hover:opacity-100',
      props.class,
    )"
    v-bind="forwardedProps"
  >
    <slot>
      <ChevronLeft class="size-4" />
    </slot>
  </RangeCalendarPrev>
</template>
