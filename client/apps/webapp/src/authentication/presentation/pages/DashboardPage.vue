<script setup lang="ts">
import { useRouter } from "vue-router";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import DashboardLayout from "@/layouts/DashboardLayout.vue";
import { useAuthStore } from "../stores/authStore.ts";

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = async () => {
	try {
		await authStore.logout();
		router.push("/login");
	} catch (error) {
		console.error("Logout failed:", error);
	}
};
</script>

<template>
  <DashboardLayout>
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <h1 class="text-3xl font-bold">Dashboard</h1>
        <Button @click="handleLogout" variant="outline">
          Logout
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Welcome back, {{ authStore.user?.firstName }}!</CardTitle>
          <CardDescription>
            You're successfully authenticated.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-2">
            <p><strong>Email:</strong> {{ authStore.user?.email }}</p>
            <p><strong>Name:</strong> {{ authStore.user?.firstName }} {{ authStore.user?.lastName }}</p>
            <p><strong>Roles:</strong> {{ authStore.user?.roles.join(', ') }}</p>
          </div>
        </CardContent>
      </Card>
    </div>
  </DashboardLayout>
</template>
