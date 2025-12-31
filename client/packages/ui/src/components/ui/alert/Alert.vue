<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { HTMLAttributes } from "vue";
import type { AlertVariants } from "./index.ts";
import { alertVariants } from "./index.ts";

const props = defineProps<{
	class?: HTMLAttributes["class"];
	variant?: AlertVariants["variant"];
}>();
</script>

<template>
  <div
    data-slot="alert"
    :class="cn(alertVariants({ variant }), props.class)"
    role="alert"
  >
    <slot />
  </div>
</template>
