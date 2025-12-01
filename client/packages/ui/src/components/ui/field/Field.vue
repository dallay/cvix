<script setup lang="ts">
import type { HTMLAttributes } from "vue";
import { cn } from "../../../lib/utils.ts";
import type { FieldVariants } from "./index.ts";
import { fieldVariants } from "./index.ts";

const props = defineProps<{
	class?: HTMLAttributes["class"];
	orientation?: FieldVariants["orientation"];
}>();
</script>

<template>
  <div
    role="group"
    data-slot="field"
    :data-orientation="orientation"
    :class="cn(
      fieldVariants({ orientation }),
      props.class,
    )"
  >
    <slot />
  </div>
</template>
