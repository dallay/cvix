<script setup lang="ts">
import { reactiveOmit } from "@vueuse/core";
import type { PinInputRootEmits, PinInputRootProps } from "reka-ui";
import { PinInputRoot, useForwardPropsEmits } from "reka-ui";
import type { HTMLAttributes } from "vue";
import { cn } from "../../../lib/utils.ts";

const props = withDefaults(
	defineProps<PinInputRootProps & { class?: HTMLAttributes["class"] }>(),
	{
		modelValue: () => [],
	},
);
const emits = defineEmits<PinInputRootEmits>();

const delegatedProps = reactiveOmit(props, "class");

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <PinInputRoot
    data-slot="pin-input"
    v-bind="forwarded" :class="cn('flex items-center gap-2 has-disabled:opacity-50 disabled:cursor-not-allowed', props.class)"
  >
    <slot />
  </PinInputRoot>
</template>
