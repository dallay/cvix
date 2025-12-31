<script setup lang="ts">
import { cn } from "@cvix/lib";
import { reactiveOmit } from "@vueuse/core";
import { ChevronRightIcon } from "lucide-vue-next";
import type { PaginationLastProps } from "reka-ui";
import { PaginationLast, useForwardProps } from "reka-ui";
import type { HTMLAttributes } from "vue";
import type { ButtonVariants } from "../button/index.ts";
import { buttonVariants } from "../button/index.ts";

const props = withDefaults(
	defineProps<
		PaginationLastProps & {
			size?: ButtonVariants["size"];
			class?: HTMLAttributes["class"];
		}
	>(),
	{
		size: "default",
	},
);

const delegatedProps = reactiveOmit(props, "class", "size");
const forwarded = useForwardProps(delegatedProps);
</script>

<template>
  <PaginationLast
    data-slot="pagination-last"
    :class="cn(buttonVariants({ variant: 'ghost', size }), 'gap-1 px-2.5 sm:pr-2.5', props.class)"
    v-bind="forwarded"
  >
    <slot>
      <span class="hidden sm:block">Last</span>
      <ChevronRightIcon />
    </slot>
  </PaginationLast>
</template>
