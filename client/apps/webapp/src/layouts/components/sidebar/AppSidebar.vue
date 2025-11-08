<script setup lang="ts">
import {
	Bell,
	CalendarClock,
	ChevronRight,
	Circle,
	CreditCard,
	Folder,
	Home,
	LifeBuoy,
	LineChart,
	LogOut,
	Settings,
	Shield,
	Users,
} from "lucide-vue-next";
import type { Component } from "vue";
import { computed, ref } from "vue";
import { RouterLink, useRoute, useRouter } from "vue-router";
import { toast } from "vue-sonner";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Collapsible,
	CollapsibleContent,
	CollapsibleTrigger,
} from "@/components/ui/collapsible";
import {
	Sidebar,
	SidebarContent,
	SidebarFooter,
	SidebarGroup,
	SidebarGroupContent,
	SidebarGroupLabel,
	SidebarHeader,
	SidebarMenu,
	SidebarMenuButton,
	SidebarMenuItem,
	SidebarMenuSub,
	SidebarMenuSubButton,
	SidebarMenuSubItem,
	SidebarRail,
} from "@/components/ui/sidebar";
import { useAuthStore } from "@/core/authentication/presentation/stores/authStore";
import { WorkspaceSelector } from "@/core/workspace";
import type { NavigationItem } from "@/shared/config/navigation";

interface Props {
	items: NavigationItem[];
}

const props = defineProps<Props>();
const authStore = useAuthStore();
const route = useRoute();
const router = useRouter();
const isLoggingOut = ref(false);

const navigationItems = computed(() => props.items ?? []);

type Team = {
	name: string;
	plan: string;
};

const teams: Team[] = [
	{ name: "Design Engineering", plan: "Enterprise" },
	{ name: "Sales & Marketing", plan: "Growth" },
	{ name: "Customer Success", plan: "Starter" },
];

const fallbackTeam: Team = teams[0] ?? { name: "Workspace", plan: "Starter" };
const activeTeam = ref<Team>(fallbackTeam);

const iconMap: Record<string, Component> = {
	home: Home,
	analytics: LineChart,
	users: Users,
	settings: Settings,
	notifications: Bell,
	billing: CreditCard,
	support: LifeBuoy,
	security: Shield,
	calendar: CalendarClock,
	projects: Folder,
};

const resolveIcon = (icon?: string) => {
	return (icon && iconMap[icon]) || Circle;
};

const isMatchingRoute = (path: string) => {
	if (!path) return false;
	const hasQuery = path.includes("?");
	const [base] = path.split("?");
	const currentPath = route.path;
	if (hasQuery) {
		return route.fullPath === path;
	}
	return currentPath === base || currentPath.startsWith(`${base}/`);
};

const isItemActive = (item: NavigationItem): boolean => {
	if (item.external) return false;
	if (item.children?.length) {
		return item.children.some((child) => isItemActive(child));
	}
	return isMatchingRoute(item.to);
};

const isChildActive = (child: NavigationItem) => {
	if (child.external) return false;
	return isMatchingRoute(child.to);
};

const userName = computed(() => {
	const user = authStore.user;
	if (!user) return "Guest";
	if (user.firstName || user.lastName) {
		return [user.firstName, user.lastName].filter(Boolean).join(" ").trim();
	}
	return user.email ?? "Member";
});

const userEmail = computed(() => authStore.user?.email ?? "No email");
const workspaceUserId = computed(() => authStore.user?.username ?? null);

const userInitials = computed(() => {
	const user = authStore.user;
	if (user?.firstName || user?.lastName) {
		return (
			`${user.firstName?.[0] ?? ""}${user.lastName?.[0] ?? ""}`.toUpperCase() ||
			"U"
		);
	}
	return user?.email?.slice(0, 2).toUpperCase() ?? "U";
});

const primaryRole = computed(() => authStore.user?.roles?.[0] ?? "Member");

type FavoriteProject = {
	name: string;
	status: string;
	url: string;
};

const favoriteProjects: FavoriteProject[] = [
	{ name: "Design System", status: "Active", url: "/dashboard" },
	{ name: "Sales Playbook", status: "Review", url: "/dashboard" },
	{ name: "Travel Portal", status: "Planning", url: "/dashboard" },
];

const userAvatarUrl = computed(() => {
	const initials = userInitials.value || "U";
	return `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(initials)}`;
});

const handleLogout = async () => {
	if (isLoggingOut.value) return;
	isLoggingOut.value = true;
	try {
		await authStore.logout();
		await router.push("/login");
	} finally {
		isLoggingOut.value = false;
	}
};

const handleWorkspaceSelected = () => {
	toast.success("Workspace switched successfully");
};
</script>

<template>
  <Sidebar collapsible="icon">
    <SidebarHeader class="gap-2 px-3 py-4">
      <div v-if="workspaceUserId" class="mt-4">
        <WorkspaceSelector
          :user-id="workspaceUserId"
          class="w-full"
          @workspace-selected="handleWorkspaceSelected"
        />
      </div>
    </SidebarHeader>

    <SidebarContent>
      <SidebarGroup>
        <SidebarGroupLabel>Platform</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <template v-for="item in navigationItems" :key="item.label">
              <Collapsible
                v-if="item.children?.length"
                :default-open="isItemActive(item)"
                class="group/collapsible"
                as-child
              >
                <SidebarMenuItem>
                  <CollapsibleTrigger as-child>
                    <SidebarMenuButton :tooltip="item.label" :is-active="isItemActive(item)">
                      <component :is="resolveIcon(item.icon)" class="size-4" />
                      <span class="truncate">{{ item.label }}</span>
                      <ChevronRight
                        class="ml-auto size-4 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-90"
                      />
                    </SidebarMenuButton>
                  </CollapsibleTrigger>
                  <CollapsibleContent>
                    <SidebarMenuSub>
                      <SidebarMenuSubItem v-for="child in item.children" :key="child.label">
                        <SidebarMenuSubButton as-child :is-active="isChildActive(child)">
                          <a
                            v-if="child.external"
                            :href="child.to"
                            target="_blank"
                            rel="noopener noreferrer"
                          >
                            <span>{{ child.label }}</span>
                          </a>
                          <RouterLink
                            v-else
                            :to="child.to"
                            class="flex w-full items-center justify-between"
                          >
                            <span>{{ child.label }}</span>
                          </RouterLink>
                        </SidebarMenuSubButton>
                      </SidebarMenuSubItem>
                    </SidebarMenuSub>
                  </CollapsibleContent>
                </SidebarMenuItem>
              </Collapsible>

              <SidebarMenuItem v-else>
                <SidebarMenuButton as-child :tooltip="item.label" :is-active="isItemActive(item)">
                  <a
                    v-if="item.external"
                    :href="item.to"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="flex w-full items-center gap-2"
                  >
                    <component :is="resolveIcon(item.icon)" class="size-4" />
                    <span class="truncate">{{ item.label }}</span>
                  </a>
                  <RouterLink v-else :to="item.to" class="flex w-full items-center gap-2">
                    <component :is="resolveIcon(item.icon)" class="size-4" />
                    <span class="truncate">{{ item.label }}</span>
                  </RouterLink>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </template>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>

      <SidebarGroup>
        <SidebarGroupLabel>Projects</SidebarGroupLabel>
        <SidebarGroupContent>
          <SidebarMenu>
            <SidebarMenuItem v-for="project in favoriteProjects" :key="project.name">
              <SidebarMenuButton tooltip="Open project" as-child>
                <RouterLink :to="project.url" class="flex w-full items-center justify-between">
                  <div class="flex items-center gap-2">
                    <Folder class="size-4 text-muted-foreground" />
                    <span class="truncate">{{ project.name }}</span>
                  </div>
                  <Badge variant="secondary" class="text-xs">{{ project.status }}</Badge>
                </RouterLink>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarGroupContent>
      </SidebarGroup>
    </SidebarContent>

    <SidebarFooter class="px-3 py-4">
      <div class="rounded-lg border bg-muted/40 p-3">
        <div class="flex items-center gap-3">
          <Avatar class="h-9 w-9 rounded-lg">
            <AvatarImage :src="userAvatarUrl" :alt="userName" />
            <AvatarFallback class="rounded-lg">{{ userInitials }}</AvatarFallback>
          </Avatar>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium leading-tight">{{ userName }}</p>
            <p class="truncate text-xs text-muted-foreground">{{ userEmail }}</p>
          </div>
        </div>
        <div class="mt-3 flex items-center justify-between text-xs text-muted-foreground">
          <span>{{ primaryRole }}</span>
          <Button
            variant="ghost"
            size="sm"
            class="h-7 gap-1 px-2 text-xs"
            :disabled="isLoggingOut"
            @click="handleLogout"
          >
            <LogOut class="size-3.5" />
            <span>Logout</span>
          </Button>
        </div>
      </div>
    </SidebarFooter>

    <SidebarRail />
  </Sidebar>
</template>
