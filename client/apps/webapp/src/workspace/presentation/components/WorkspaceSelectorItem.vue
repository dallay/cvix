<script setup lang="ts">
import { Check } from "lucide-vue-next";
import { computed } from "vue";

import type { Workspace } from "../../../workspace/domain/WorkspaceEntity";

interface Props {
	workspace: Workspace;
	isSelected?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
	isSelected: false,
});

const hasDescription = computed(
	() => props.workspace.description && props.workspace.description.length > 0,
);
</script>

<template>
	<div
		:class="[
			'flex items-center justify-between px-3 py-2 cursor-pointer',
			'hover:bg-accent hover:text-accent-foreground',
			'rounded-sm transition-colors',
			{ selected: isSelected, 'bg-accent': isSelected }
		]"
		role="option"
		:aria-selected="isSelected"
		:data-workspace-id="workspace.id"
	>
		<div class="flex flex-col gap-1 flex-1 min-w-0">
			<div class="flex items-center gap-2">
				<span
					data-testid="workspace-name"
					class="font-medium text-sm truncate"
				>
					{{ workspace.name }}
				</span>
				<span
					v-if="workspace.isDefault"
					data-testid="default-badge"
					class="inline-flex items-center px-1.5 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-primary"
				>
					<span data-testid="default-badge-in-selector">Default</span>
				</span>
			</div>
			<p
				v-if="hasDescription"
				data-testid="workspace-description"
				class="text-xs text-muted-foreground truncate"
			>
				{{ workspace.description }}
			</p>
		</div>
		<Check
			v-if="isSelected"
			data-testid="check-icon"
			class="h-4 w-4 ml-2 shrink-0"
		/>
	</div>
</template>

<style scoped>
.selected {
	font-weight: 500;
}
</style>
