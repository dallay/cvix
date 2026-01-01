<script setup lang="ts">
import { cn } from "@cvix/lib";
import { reactiveOmit } from "@vueuse/core";
import type { PrimitiveProps } from "reka-ui";
import { Primitive } from "reka-ui";
import type { HTMLAttributes } from "vue";
import type { BadgeVariants } from "./index.ts";
import { badgeVariants } from "./index.ts";

const props = defineProps<
	PrimitiveProps & {
		variant?: BadgeVariants["variant"];
		class?: HTMLAttributes["class"];
	}
>();

const delegatedProps = reactiveOmit(props, "class");
</script>

<template>
  <Primitive
    data-slot="badge"
    :class="cn(badgeVariants({ variant }), props.class)"
    v-bind="delegatedProps"
  >
    <slot />
  </Primitive>
</template>
