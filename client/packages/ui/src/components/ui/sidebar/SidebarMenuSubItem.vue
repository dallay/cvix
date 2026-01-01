<script setup lang="ts">
import { cn } from "@cvix/lib";
import type { HTMLAttributes } from "vue";

const props = defineProps<{
	class?: HTMLAttributes["class"];
}>();
</script>

<template>
  <li
    data-slot="sidebar-menu-sub-item"
    data-sidebar="menu-sub-item"
    :class="cn('group/menu-sub-item relative', props.class)"
  >
    <slot />
  </li>
</template>
