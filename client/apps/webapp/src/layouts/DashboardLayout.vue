<script setup lang="ts">
import { computed } from "vue";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import AppSidebar from "@/layouts/components/sidebar/AppSidebar.vue";
import { getNavigationItems } from "@/shared/config/navigation";
import AppHeader from "./components/AppHeader.vue";
import SimpleLayout from "./SimpleLayout.vue";

// Get navigation items reactively
const navigationItems = computed(() => getNavigationItems());
</script>

<template>
  <SidebarProvider>
    <AppSidebar :items="navigationItems" />
    <SidebarInset>
      <AppHeader />
      <SimpleLayout>
        <slot />
      </SimpleLayout>
    </SidebarInset>
  </SidebarProvider>
</template>
