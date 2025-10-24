<script setup lang="ts">
import { computed } from "vue";
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar";
import AppSidebar from "@/layouts/components/sidebar/AppSidebar.vue";
import { getNavigationItems } from "@/shared/config/navigation";
import AppHeader from "./components/AppHeader.vue";

// Keep the sidebar navigation reactive so updates propagate automatically.
const navigationItems = computed(() => getNavigationItems());
</script>

<template>
  <SidebarProvider class="bg-muted/30">
    <AppSidebar :items="navigationItems" />
    <SidebarInset class="bg-background">
      <AppHeader />
      <div class="flex flex-1 flex-col">
        <slot />
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>
