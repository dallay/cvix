<script lang="ts" setup>
import { cn } from "@cvix/lib";
import { ErrorMessage } from "vee-validate";
import type { HTMLAttributes } from "vue";
import { toValue } from "vue";
import { useFormField } from "./useFormField.ts";

const props = defineProps<{
	class?: HTMLAttributes["class"];
}>();

const { name, formMessageId } = useFormField();
</script>

<template>
  <ErrorMessage
    :id="formMessageId"
    data-slot="form-message"
    as="p"
    :name="toValue(name)"
    :class="cn('text-destructive text-sm', props.class)"
  />
</template>
