<script setup lang="ts">
import {
	Activity,
	Clock,
	ShieldCheck,
	Users as UsersIcon,
} from "lucide-vue-next";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { toast } from "vue-sonner"; // Import toast from vue-sonner
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import DashboardLayout from "@/layouts/DashboardLayout.vue";
import { useAuthStore } from "../stores/authStore.ts";

const authStore = useAuthStore();
const router = useRouter();
const isLoggingOut = ref(false);

const userRolesLabel = computed(() => {
	const roles = authStore.user?.roles;
	if (!roles || roles.length === 0) return "No roles assigned";
	return roles.join(", ");
});

const hasRoles = computed(() => (authStore.user?.roles?.length ?? 0) > 0);

const userDisplayName = computed(() => {
	const user = authStore.user;
	if (!user) return "";
	if (user.firstName || user.lastName) {
		return [user.firstName, user.lastName].filter(Boolean).join(" ").trim();
	}
	return user.email ?? "";
});

const greetingHeadline = computed(() => {
	const name = userDisplayName.value;
	return name ? `Welcome back, ${name}` : "Welcome back";
});

const overviewCards = computed(() => [
	{
		title: "Active Sessions",
		value: "128",
		subtext: "+12.4% vs last month",
		icon: Activity,
	},
	{
		title: "Team Members",
		value: "24",
		subtext: "3 pending invites",
		icon: UsersIcon,
	},
	{
		title: "Response Time",
		value: "184 ms",
		subtext: "API average today",
		icon: Clock,
	},
	{
		title: "Security Score",
		value: "A-",
		subtext: "No incidents in 90 days",
		icon: ShieldCheck,
	},
]);

const usageSummary = [
	{ label: "API Requests", value: "62%", progress: 62 },
	{ label: "Storage", value: "480 GB / 1 TB", progress: 48 },
	{ label: "Seats Used", value: "18 / 25", progress: 72 },
];

const recentActivity = [
	{
		id: 1,
		title: "Billing automation deployed",
		description: "Ana pushed new workflow rules for invoices.",
		time: "2 hours ago",
		initials: "AS",
		status: "Update",
	},
	{
		id: 2,
		title: "New teammate invited",
		description: "Miguel sent an invite to finance@acme.com.",
		time: "Yesterday",
		initials: "MG",
		status: "Invite",
	},
	{
		id: 3,
		title: "Quarterly report exported",
		description: "Export completed for Q3 revenue insights.",
		time: "2 days ago",
		initials: "HQ",
		status: "Report",
	},
	{
		id: 4,
		title: "Support SLA breached",
		description: "Escalated ticket #34921 resolved in 8 min.",
		time: "4 days ago",
		initials: "CS",
		status: "Incident",
	},
];

const projects = [
	{
		name: "Design System",
		description: "Component audit and accessibility review.",
		status: "Active",
		updated: "2 days ago",
	},
	{
		name: "RevOps Dashboard",
		description: "Realtime pipeline metrics for leadership.",
		status: "Review",
		updated: "5 days ago",
	},
	{
		name: "Mobile Onboarding",
		description: "Streamlined identity verification flow.",
		status: "Planning",
		updated: "1 day ago",
	},
];

const handleLogout = async () => {
	if (isLoggingOut.value) return;
	isLoggingOut.value = true;
	try {
		await authStore.logout();
		await router.push("/login");
	} catch (error) {
		console.error("Logout failed:", error);
		toast.error(
			"Logout failed" +
				(error instanceof Error && error.message ? `: ${error.message}` : ""),
		);
	} finally {
		isLoggingOut.value = false;
	}
};
</script>

<template>
  <DashboardLayout>
    <div class="flex flex-1 flex-col gap-6 p-4 md:gap-8 md:p-8">
      <section class="flex flex-col gap-4">
        <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 class="text-3xl font-semibold tracking-tight">Dashboard</h1>
            <p class="text-muted-foreground">
              A snapshot of your workspace health, activity, and usage.
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-3">
            <Button variant="outline" size="sm">
              Invite teammates
            </Button>
            <Button size="sm">
              New project
            </Button>
          </div>
        </div>

        <Card class="border-dashed">
          <CardContent class="flex flex-col gap-4 p-4 md:flex-row md:items-center md:justify-between">
            <div class="space-y-1">
              <p class="text-sm text-muted-foreground">{{ greetingHeadline }}</p>
              <h2 class="text-xl font-semibold leading-tight">You're signed in and ready to go.</h2>
              <p class="text-sm text-muted-foreground">
                Using <span class="font-medium">{{ authStore.user?.email ?? "unknown email" }}</span>
                <template v-if="hasRoles">
                  with roles: {{ userRolesLabel }}.
                </template>
                <template v-else>
                  with no roles assigned yet.
                </template>
              </p>
            </div>
            <div class="flex flex-wrap items-center gap-3">
              <Badge v-if="hasRoles" variant="secondary">
                {{ userRolesLabel }}
              </Badge>
              <Badge v-else variant="outline">
                Pending role setup
              </Badge>
              <Button
                variant="ghost"
                size="sm"
                :disabled="isLoggingOut"
                @click="handleLogout"
              >
                Sign out
              </Button>
            </div>
          </CardContent>
        </Card>
      </section>

      <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Card v-for="card in overviewCards" :key="card.title" class="shadow-sm">
          <CardHeader class="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle class="text-sm font-medium">{{ card.title }}</CardTitle>
            <component :is="card.icon" class="size-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold">{{ card.value }}</div>
            <p class="text-xs text-muted-foreground">{{ card.subtext }}</p>
          </CardContent>
        </Card>
      </section>

      <section class="grid gap-4 lg:grid-cols-7">
        <Card class="lg:col-span-4">
          <CardHeader class="space-y-1">
            <CardTitle>Team activity</CardTitle>
            <CardDescription>Latest actions across your workspace.</CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <div
              v-for="activity in recentActivity"
              :key="activity.id"
              class="flex items-start gap-3 rounded-lg border border-dashed p-3"
            >
              <Avatar class="h-10 w-10 rounded-lg">
                <AvatarFallback class="rounded-lg">{{ activity.initials }}</AvatarFallback>
              </Avatar>
              <div class="flex-1 space-y-1">
                <p class="text-sm font-medium leading-none">{{ activity.title }}</p>
                <p class="text-sm text-muted-foreground">{{ activity.description }}</p>
                <p class="text-xs text-muted-foreground">{{ activity.time }}</p>
              </div>
              <Badge variant="outline">{{ activity.status }}</Badge>
            </div>
          </CardContent>
        </Card>

        <Card class="lg:col-span-3">
          <CardHeader class="space-y-1">
            <CardTitle>Usage summary</CardTitle>
            <CardDescription>Monitor key resource consumption.</CardDescription>
          </CardHeader>
          <CardContent class="space-y-5">
            <div v-for="item in usageSummary" :key="item.label" class="space-y-2">
              <div class="flex items-center justify-between text-sm font-medium">
                <span>{{ item.label }}</span>
                <span>{{ item.value }}</span>
              </div>
              <Progress :value="item.progress" class="h-2" />
            </div>
            <div class="rounded-lg border bg-muted/40 p-4 text-sm text-muted-foreground">
              You're on the <span class="font-medium">Pro</span> plan. Upgrade to unlock advanced analytics
              and 24/7 support.
            </div>
          </CardContent>
        </Card>
      </section>

      <Card>
        <CardHeader class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <CardTitle>Projects</CardTitle>
            <CardDescription>Your most active workstreams.</CardDescription>
          </div>
          <div class="flex items-center gap-2">
            <Button variant="outline" size="sm">
              Share
            </Button>
            <Button size="sm">
              Create project
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div class="grid gap-4 md:grid-cols-3">
            <div
              v-for="project in projects"
              :key="project.name"
              class="rounded-lg border bg-background p-4 shadow-sm transition hover:border-primary"
            >
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium">{{ project.name }}</span>
                <Badge :variant="project.status === 'Active' ? 'default' : 'secondary'" class="text-xs">
                  {{ project.status }}
                </Badge>
              </div>
              <p class="mt-2 text-sm text-muted-foreground">{{ project.description }}</p>
              <div class="mt-4 flex items-center justify-between text-xs text-muted-foreground">
                <span>Updated {{ project.updated }}</span>
                <Button variant="ghost" size="sm">
                  Open
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </DashboardLayout>
</template>
