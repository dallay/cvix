<script lang="ts" setup>
import { cn } from "@cvix/lib";
import type { HTMLAttributes } from "vue";
import { useFormField } from "./useFormField.ts";

const props = defineProps<{
	class?: HTMLAttributes["class"];
}>();

const { formDescriptionId } = useFormField();
</script>

<template>
  <p
    :id="formDescriptionId"
    data-slot="form-description"
    :class="cn('text-muted-foreground text-sm', props.class)"
  >
    <slot />
  </p>
</template>
