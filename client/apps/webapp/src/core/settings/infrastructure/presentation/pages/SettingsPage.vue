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
      <h1 class="text-3xl font-bold">{{$t('settings.title')}}</h1>
      <p class="text-muted-foreground mt-2">
        {{$t('settings.description')}}
      </p>
    </div>

    <Tabs :model-value="currentSection">
      <TabsList class="grid w-full grid-cols-3">
        <TabsTrigger value="storage">{{$t('settings.tabs.storage')}}</TabsTrigger>
        <TabsTrigger value="general">{{$t('settings.tabs.general')}}</TabsTrigger>
        <TabsTrigger value="privacy">{{$t('settings.tabs.privacy')}}</TabsTrigger>
      </TabsList>

      <TabsContent value="storage" class="mt-6">
        <StorageSelector />
      </TabsContent>

      <TabsContent value="general" class="mt-6">
        <Card>
          <CardHeader>
            <CardTitle>{{$t('settings.general.title')}}</CardTitle>
            <CardDescription>
              {{$t('settings.general.description')}}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p class="text-muted-foreground">
              {{$t('settings.general.comingSoon')}}
            </p>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="privacy" class="mt-6">
        <Card>
          <CardHeader>
            <CardTitle>{{$t('settings.privacy.title')}}</CardTitle>
            <CardDescription>
              {{$t('settings.privacy.description')}}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <p class="text-muted-foreground">
              {{$t('settings.privacy.comingSoon')}}
            </p>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
  </DashboardLayout>
</template>
