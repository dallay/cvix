<script setup lang="ts">
import { onMounted } from "vue";
import { RouterView } from "vue-router";
import { useAuthStore } from "@/core/authentication/presentation/stores/authStore.ts";
import { useSettingsStore } from "@/core/settings";

const authStore = useAuthStore();
const settingsStore = useSettingsStore();

onMounted(() => {
	// Initialize settings store to load user preferences
	settingsStore.loadSettings();

	// Initialize auth store and restore session if valid
	authStore.initialize();
});
</script>

<template>
  <RouterView />
</template>
