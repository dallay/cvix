<script setup lang="ts">
import {
	AlertCircle,
	Building2,
	ChevronsUpDown,
	Loader2,
	Sparkles,
	Users,
} from "lucide-vue-next";
import type { Component } from "vue";
import { computed, ref } from "vue";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuShortcut,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useWorkspaceSelection } from "@/core/workspace";

interface Props {
	userId: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
	"workspace-selected": [workspaceId: string];
}>();

// Composable
const {
	workspaces,
	currentWorkspace,
	isLoading,
	error,
	hasWorkspaces,
	selectWorkspace,
} = useWorkspaceSelection();

// Local state
const isSwitching = ref(false);

// Get workspace icon
function getWorkspaceIcon(workspaceName?: string): Component {
	if (!workspaceName) return Sparkles;
	const name = workspaceName.toLowerCase();
	if (name.includes("personal") || name.includes("default")) {
		return Users;
	}
	if (name.includes("enterprise") || name.includes("business")) {
		return Building2;
	}
	return Sparkles;
}

const currentWorkspaceIcon = computed(() =>
	getWorkspaceIcon(currentWorkspace.value?.name),
);

// Methods
async function handleWorkspaceClick(workspaceId: string) {
	if (workspaceId === currentWorkspace.value?.id || isSwitching.value) {
		return;
	}

	isSwitching.value = true;

	try {
		await selectWorkspace(workspaceId, props.userId);
		emit("workspace-selected", workspaceId);
	} catch (err) {
		console.error("Failed to switch workspace:", err);
	} finally {
		isSwitching.value = false;
	}
}
</script>

<template>
	<div data-testid="workspace-selector" class="w-full">
		<!-- Error State -->
		<div
			v-if="error"
			class="flex items-center gap-2 text-sm text-destructive"
		>
			<AlertCircle data-testid="error-icon" class="h-4 w-4 shrink-0" />
			<span>{{ error.message }}</span>
		</div>

		<!-- Loading State -->
		<div
			v-else-if="isLoading"
			data-testid="workspace-loading"
			class="flex items-center gap-2 text-sm text-muted-foreground"
		>
			<Loader2 class="h-4 w-4 animate-spin shrink-0" />
			<span>Loading workspaces...</span>
		</div>

		<!-- Empty State -->
		<div
			v-else-if="!hasWorkspaces"
			class="text-sm text-muted-foreground"
		>
			No workspaces available
		</div>

		<DropdownMenu v-else>
			<DropdownMenuTrigger as-child>
				<button
					type="button"
					class="flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-left text-sm outline-none ring-ring transition-colors hover:bg-accent hover:text-accent-foreground focus-visible:ring-2 data-[state=open]:bg-accent data-[state=open]:text-accent-foreground"
					:disabled="isSwitching"
					:aria-label="`Select workspace. Currently selected: ${currentWorkspace?.name || 'None'}`"
				>
					<template v-if="isSwitching">
						<Loader2 class="h-4 w-4 animate-spin" />
						<span>Switching...</span>
					</template>
					<template v-else-if="currentWorkspace">
						<div class="flex aspect-square size-8 items-center justify-center rounded-lg bg-primary text-primary-foreground">
							<component :is="currentWorkspaceIcon" class="size-4" />
						</div>
						<div class="grid flex-1 text-left text-sm leading-tight">
							<span class="truncate font-semibold">
								{{ currentWorkspace.name }}
							</span>
							<span v-if="currentWorkspace.description" class="truncate text-xs text-muted-foreground">
								{{ currentWorkspace.description }}
							</span>
						</div>
						<ChevronsUpDown class="ml-auto size-4" />
					</template>
					<template v-else>
						<span class="text-muted-foreground">
							Select workspace
						</span>
					</template>
				</button>
			</DropdownMenuTrigger>

			<DropdownMenuContent
				class="w-64 rounded-lg"
				align="start"
				side="right"
				:side-offset="8"
			>
				<DropdownMenuLabel class="text-xs text-muted-foreground">
					Workspaces
				</DropdownMenuLabel>
				<DropdownMenuItem
					v-for="(workspace, index) in workspaces"
					:key="workspace.id"
					class="gap-3 p-2 cursor-pointer"
					:class="{ 'bg-accent': workspace.id === currentWorkspace?.id }"
					:disabled="workspace.id === currentWorkspace?.id"
					@click="handleWorkspaceClick(workspace.id)"
				>
					<div class="flex size-8 items-center justify-center rounded-md bg-primary/10">
						<component :is="getWorkspaceIcon(workspace.name)" class="size-4 shrink-0 text-primary" />
					</div>
					<div class="grid flex-1 text-left text-sm leading-tight">
						<span class="truncate font-medium">
							{{ workspace.name }}
						</span>
						<span v-if="workspace.description" class="truncate text-xs text-muted-foreground">
							{{ workspace.description }}
						</span>
					</div>
					<DropdownMenuShortcut v-if="index < 9" class="ml-auto">
						⌘{{ index + 1 }}
					</DropdownMenuShortcut>
				</DropdownMenuItem>
			</DropdownMenuContent>
		</DropdownMenu>
	</div>
</template>

<style scoped>
/* Ensure proper text truncation */
.truncate {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
</style>
