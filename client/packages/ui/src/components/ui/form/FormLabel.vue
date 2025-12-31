<script lang="ts" setup>
import { cn } from "@cvix/lib";
import type { LabelProps } from "reka-ui";
import type { HTMLAttributes } from "vue";
import { Label } from "../label/index.ts";
import { useFormField } from "./useFormField.ts";

const props = defineProps<LabelProps & { class?: HTMLAttributes["class"] }>();

const { error, formItemId } = useFormField();
</script>

<template>
  <Label
    data-slot="form-label"
    :data-error="!!error"
    :class="cn(
      'data-[error=true]:text-destructive',
      props.class,
    )"
    :for="formItemId"
  >
    <slot />
  </Label>
</template>
