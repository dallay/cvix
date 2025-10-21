<script setup lang="ts">
import { onMounted } from "vue";
import { RouterView } from "vue-router";
import { useAuthStore } from "./authentication/presentation/stores/authStore.ts";

const authStore = useAuthStore();

onMounted(() => {
	// Initialize auth store and restore session if valid
	authStore.initialize();
});
</script>

<template>
  <RouterView />
</template>

<style scoped></style>
