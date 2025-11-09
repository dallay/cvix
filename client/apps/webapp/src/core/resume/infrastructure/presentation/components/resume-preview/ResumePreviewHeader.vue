<script setup lang="ts">
import type { Basics } from "@/core/resume/domain/Resume";

interface Props {
	basics: Basics;
}

const props = defineProps<Props>();

/**
 * Formats location into a single line string
 */
function formatLocation(location: Basics["location"]): string {
	if (!location) return "";
	const parts = [location.city, location.region, location.countryCode].filter(
		Boolean,
	);
	return parts.join(", ");
}
</script>

<template>
  <header class="bg-card text-card-foreground p-8 space-y-4 border-b border-border">
    <!-- Name and Title -->
    <div class="space-y-1">
      <h1 class="text-3xl font-bold">{{ basics.name }}</h1>
      <p v-if="basics.label" class="text-lg opacity-90">{{ basics.label }}</p>
    </div>

    <!-- Contact Information -->
    <div class="flex flex-wrap gap-x-6 gap-y-2 text-sm opacity-90">
      <div v-if="basics.email" class="flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
        </svg>
        <a :href="`mailto:${basics.email}`" class="hover:underline">{{ basics.email }}</a>
      </div>

      <div v-if="basics.phone" class="flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
        </svg>
        <span>{{ basics.phone }}</span>
      </div>

      <div v-if="basics.location" class="flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/>
        </svg>
        <span>{{ formatLocation(basics.location) }}</span>
      </div>

      <div v-if="basics.url" class="flex items-center gap-2">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"/>
        </svg>
        <a :href="basics.url" target="_blank" rel="noopener noreferrer" class="hover:underline">
          {{ basics.url.replace(/^https?:\/\//, '') }}
        </a>
      </div>
    </div>

    <!-- Social Profiles -->
    <div v-if="basics.profiles?.length" class="flex flex-wrap gap-3 pt-2">
      <a
          v-for="(profile, index) in basics.profiles"
          :key="index"
          :href="profile.url"
          target="_blank"
          rel="noopener noreferrer"
          class="flex items-center gap-2 text-sm opacity-90 hover:opacity-100 hover:underline transition-opacity"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
        </svg>
        {{ profile.network }}
      </a>
    </div>
  </header>
</template>
