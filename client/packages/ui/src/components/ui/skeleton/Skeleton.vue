<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { HTMLAttributes } from "vue";

interface SkeletonProps {
	class?: HTMLAttributes["class"];
}

const props = defineProps<SkeletonProps>();
</script>

<template>
  <div
    data-slot="skeleton"
    :class="cn('animate-pulse rounded-md bg-primary/10', props.class)"
  />
</template>
