<script setup lang="ts">
interface Props {
	/**
	 * The section title to display
	 */
	title: string;
}

const props = defineProps<Props>();
</script>

<template>
  <section class="space-y-3">
    <h2 class="text-lg font-semibold text-foreground border-b border-border pb-2">
      {{ title }}
    </h2>
    <div>
      <slot/>
    </div>
  </section>
</template>
