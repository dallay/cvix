<script setup lang="ts">
import { AlertCircle, Loader2 } from "lucide-vue-next";
import { computed, ref } from "vue";
import {
	Select,
	SelectContent,
	SelectGroup,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { useWorkspaceSelection } from "@/workspace";
import WorkspaceSelectorItem from "./WorkspaceSelectorItem.vue";

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
const isOpen = ref(false);
const isSwitching = ref(false);

// Computed
const selectedValue = computed(
	() => currentWorkspace.value?.id ?? "placeholder",
);

// Methods
async function handleSelect(workspaceId: unknown) {
	if (
		typeof workspaceId !== "string" ||
		!workspaceId ||
		workspaceId === "placeholder" ||
		workspaceId === currentWorkspace.value?.id
	) {
		return;
	}

	isSwitching.value = true;

	try {
		await selectWorkspace(workspaceId, props.userId);
		emit("workspace-selected", workspaceId);
		isOpen.value = false;
	} catch (err) {
		console.error("Failed to switch workspace:", err);
	} finally {
		isSwitching.value = false;
	}
}

function handleOpenChange(open: boolean) {
	isOpen.value = open;
}
</script>

<template>
	<div data-testid="workspace-selector" class="w-full max-w-xs">
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

		<!-- Workspace Selector -->
		<Select
			v-else
			:model-value="selectedValue"
			:open="isOpen"
			:disabled="isSwitching"
			@update:model-value="handleSelect"
			@update:open="handleOpenChange"
		>
			<SelectTrigger
				role="combobox"
				:aria-expanded="isOpen"
				:aria-disabled="isSwitching"
				:aria-label="`Select workspace. Currently selected: ${currentWorkspace?.name || 'None'}`"
				aria-haspopup="listbox"
				class="w-full"
			>
				<SelectValue>
					<div v-if="isSwitching" class="flex items-center gap-2">
						<Loader2 class="h-4 w-4 animate-spin" />
						<span>Switching...</span>
					</div>
					<div v-else-if="currentWorkspace" class="flex items-center gap-2">
						<span class="truncate">{{ currentWorkspace.name }}</span>
						<span
							v-if="currentWorkspace.isDefault"
							class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-primary shrink-0"
						>
							Default
						</span>
					</div>
					<span v-else class="text-muted-foreground">
						Select workspace
					</span>
				</SelectValue>
			</SelectTrigger>

			<SelectContent role="listbox">
				<SelectGroup>
					<SelectItem
						v-for="workspace in workspaces"
						:key="workspace.id"
						:value="workspace.id"
						class="cursor-pointer"
					>
						<WorkspaceSelectorItem
							:workspace="workspace"
							:is-selected="workspace.id === currentWorkspace?.id"
						/>
					</SelectItem>
				</SelectGroup>
			</SelectContent>
		</Select>
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
