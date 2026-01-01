<script setup lang="ts">
import { cn } from "@cvix/lib";
import { AvatarRoot } from "reka-ui";
import type { HTMLAttributes } from "vue";

const props = defineProps<{
	class?: HTMLAttributes["class"];
}>();
</script>

<template>
  <AvatarRoot
    data-slot="avatar"
    :class="cn('relative flex size-8 shrink-0 overflow-hidden rounded-full', props.class)"
  >
    <slot />
  </AvatarRoot>
</template>
