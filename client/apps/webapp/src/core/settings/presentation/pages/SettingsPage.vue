<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { StorageSelector } from "@/core/settings";
import DashboardLayout from "@/layouts/DashboardLayout.vue";

const route = useRoute();

const currentSection = computed(() => {
	return (route.query.section as string) || "storage";
});
</script>
<template >
  <DashboardLayout>
	<div class="container mx-auto max-w-4xl py-8">
    <div class="mb-8">
      <h1 class="text-3xl font-bold">Settings</h1>
      <p class="text-muted-foreground mt-2">
        Manage your application preferences and settings
      </p>
    </div>

    <Tabs :model-value="currentSection">
      <TabsList class="grid w-full grid-cols-3">
        <TabsTrigger value="storage">Storage</TabsTrigger>
        <TabsTrigger value="general">General</TabsTrigger>
        <TabsTrigger value="privacy">Privacy</TabsTrigger>
      </TabsList>

      <TabsContent value="storage" class="mt-6">
        <StorageSelector />
      </TabsContent>

      <TabsContent value="general" class="mt-6">
        <Card>
          <CardHeader>
            <CardTitle>General Settings</CardTitle>
            <CardDescription>
              Configure general application preferences
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p class="text-muted-foreground">
              General settings coming soon...
            </p>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="privacy" class="mt-6">
        <Card>
          <CardHeader>
            <CardTitle>Privacy Settings</CardTitle>
            <CardDescription>
              Manage your privacy and data preferences
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p class="text-muted-foreground">
              Privacy settings coming soon...
            </p>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
  </DashboardLayout>
</template>
