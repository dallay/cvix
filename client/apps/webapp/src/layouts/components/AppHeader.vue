<script setup>
import { storeToRefs } from "pinia";
import { computed, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { useAuthStore } from "@/authentication/infrastructure/store";
import MainMenuNav from "@/components/MainMenuNav.vue";
import ThemeSwitcher from "@/components/ThemeSwitcher.vue";
import UserNav from "@/components/UserNav.vue";
import {
	Breadcrumb,
	BreadcrumbItem,
	BreadcrumbLink,
	BreadcrumbList,
	BreadcrumbPage,
	BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import { Separator } from "@/components/ui/separator";
import { SidebarTrigger } from "@/components/ui/sidebar";
import LanguageSwitcher from "./LanguageSwitcher.vue";

const authStore = useAuthStore();
const { isAuthenticated } = storeToRefs(authStore);
const router = useRouter();
const route = useRoute();
const isLoggingOut = ref(false);

const handleLogout = async () => {
	if (isLoggingOut.value) return;

	isLoggingOut.value = true;
	try {
		await authStore.logoutAsync();
		toast.success("Successfully logged out");
		await router.push("/login");
	} catch (error) {
		console.error("Error during logout:", error);
		toast.error("Logout failed");
	} finally {
		isLoggingOut.value = false;
	}
};

// Dynamically generate breadcrumbs from route.matched
const breadcrumbs = computed(() => {
	return route.matched
		.filter((r) => r.name && r.path !== "/")
		.map((r, idx, arr) => {
			return {
				name: r.name,
				path: r.path,
				isLast: idx === arr.length - 1,
				meta: r.meta || {},
			};
		});
});
</script>

<template>
  <header
    class="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12 bg-white border-b border-gray-200 sticky top-0 z-50 dark:bg-gray-900 dark:border-gray-700"
  >
    <div class="flex items-center gap-2 px-4 flex-1">
      <SidebarTrigger class="-ml-1" />
      <Separator orientation="vertical" class="mr-2 h-4" />
      <Breadcrumb>
        <BreadcrumbList>
          <BreadcrumbItem>
            <BreadcrumbLink href="/">Home</BreadcrumbLink>
          </BreadcrumbItem>
          <template v-for="(crumb, idx) in breadcrumbs" :key="crumb.path">
            <BreadcrumbSeparator />
            <BreadcrumbItem>
              <template v-if="!crumb.isLast">
                <BreadcrumbLink :href="crumb.path">
                  {{ crumb.name }}
                </BreadcrumbLink>
              </template>
              <template v-else>
                <BreadcrumbPage>
                  {{ crumb.name }}
                </BreadcrumbPage>
              </template>
            </BreadcrumbItem>
          </template>
        </BreadcrumbList>
      </Breadcrumb>
    </div>
    <div class="ml-auto flex items-center space-x-4 px-4">
      <MainMenuNav class="mx-6" />
      <LanguageSwitcher />
      <ThemeSwitcher />
      <UserNav variant="compact" />
    </div>
  </header>
</template>
