<script setup lang="ts">
import { SidebarInset, SidebarProvider } from "@cvix/ui/components/ui/sidebar";
import { computed } from "vue";
import AppSidebar from "@/layouts/components/sidebar/AppSidebar.vue";
import { getNavigationItems } from "@/shared/config/navigation";
import AppHeader from "./components/AppHeader.vue";

const navigationItems = computed(() => getNavigationItems());
</script>

<template>
  <SidebarProvider class="bg-background">
    <AppSidebar :items="navigationItems" />
    <SidebarInset class="bg-background">
      <AppHeader />
      <div class="flex flex-1 flex-col">
        <slot />
      </div>
    </SidebarInset>
  </SidebarProvider>
</template>
