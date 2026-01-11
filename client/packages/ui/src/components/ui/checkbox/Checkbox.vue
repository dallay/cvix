<script setup lang="ts">
import { cn } from "@cvix/lib";
import { reactiveOmit } from "@vueuse/core";
import { Check, Minus } from "lucide-vue-next";
import type { CheckboxRootEmits, CheckboxRootProps } from "reka-ui";
import { CheckboxIndicator, CheckboxRoot, useForwardPropsEmits } from "reka-ui";
import type { HTMLAttributes } from "vue";
import { computed } from "vue";

const props = defineProps<
	CheckboxRootProps & {
		class?: HTMLAttributes["class"];
		/**
		 * Controlled checked state.
		 * This is an alias for `modelValue` to match common checkbox API expectations.
		 * Internally, we forward this to CheckboxRoot's `modelValue` prop.
		 */
		checked?: boolean | "indeterminate";
	}
>();

const emits = defineEmits<
	CheckboxRootEmits & {
		/**
		 * Emitted when the checked state changes.
		 * This mirrors `update:modelValue` for API consistency.
		 */
		"update:checked": [value: boolean | "indeterminate"];
	}
>();

// Exclude 'class' and 'checked' from delegated props
// 'class' is handled manually for styling
// 'checked' is our custom prop that we'll map to modelValue
const delegatedProps = reactiveOmit(props, "class", "checked");

const forwarded = useForwardPropsEmits(delegatedProps, emits);

// Map our checked prop to modelValue and emit both events for compatibility
const handleUpdate = (value: boolean | "indeterminate") => {
	emits("update:checked", value);
	emits("update:modelValue", value);
};

// Determine which icon to show based on checked state
const isIndeterminate = computed(() => props.checked === "indeterminate");
</script>

<template>
  <CheckboxRoot
    data-slot="checkbox"
    v-bind="forwarded"
    :model-value="checked"
    @update:model-value="handleUpdate"
    :class="
      cn('peer border-input data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground data-[state=checked]:border-primary data-[state=indeterminate]:bg-primary data-[state=indeterminate]:text-primary-foreground data-[state=indeterminate]:border-primary focus-visible:border-ring focus-visible:ring-ring/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive size-4 shrink-0 rounded-[4px] border shadow-xs transition-shadow outline-none focus-visible:ring-[3px] disabled:cursor-not-allowed disabled:opacity-50',
         props.class)"
  >
    <CheckboxIndicator
      data-slot="checkbox-indicator"
      class="flex items-center justify-center text-current transition-none"
    >
      <slot>
        <!-- Show minus icon for indeterminate, check for checked -->
        <Minus v-if="isIndeterminate" class="size-3.5" />
        <Check v-else class="size-3.5" />
      </slot>
    </CheckboxIndicator>
  </CheckboxRoot>
</template>
