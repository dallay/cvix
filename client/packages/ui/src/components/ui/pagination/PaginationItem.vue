<script setup lang="ts">
import { cn } from "@cvix/lib";
import { reactiveOmit } from "@vueuse/core";
import type { PaginationListItemProps } from "reka-ui";
import { PaginationListItem } from "reka-ui";
import type { HTMLAttributes } from "vue";
import type { ButtonVariants } from "../button/index.ts";
import { buttonVariants } from "../button/index.ts";

const props = withDefaults(
	defineProps<
		PaginationListItemProps & {
			size?: ButtonVariants["size"];
			class?: HTMLAttributes["class"];
			isActive?: boolean;
		}
	>(),
	{
		size: "icon",
	},
);

const delegatedProps = reactiveOmit(props, "class", "size", "isActive");
</script>

<template>
  <PaginationListItem
    data-slot="pagination-item"
    v-bind="delegatedProps"
    :class="cn(
      buttonVariants({
        variant: isActive ? 'outline' : 'ghost',
        size,
      }),
      props.class)"
  >
    <slot />
  </PaginationListItem>
</template>
