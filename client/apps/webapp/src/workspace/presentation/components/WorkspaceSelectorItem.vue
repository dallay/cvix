<script setup lang="ts">
import { Building2, Check, Sparkles, Users } from "lucide-vue-next";
import { computed } from "vue";

import type { Workspace } from "../../../workspace/domain/WorkspaceEntity";

interface Props {
	workspace: Workspace;
	isSelected?: boolean;
	keyboardShortcut?: string;
}

const props = withDefaults(defineProps<Props>(), {
	isSelected: false,
	keyboardShortcut: undefined,
});

const hasDescription = computed(
	() => props.workspace.description && props.workspace.description.length > 0,
);

// Generate workspace icon based on name or index
const WorkspaceIcon = computed(() => {
	const name = props.workspace.name.toLowerCase();
	if (name.includes("personal") || name.includes("default")) {
		return Users;
	}
	if (name.includes("enterprise") || name.includes("business")) {
		return Building2;
	}
	return Sparkles;
});
</script>

<template>
	<div
		:class="[
			'flex items-center gap-3 py-2.5 px-2 cursor-pointer',
			'hover:bg-accent/50 rounded-md transition-all duration-200',
			{ 'bg-accent/30': isSelected }
		]"
		role="option"
		:aria-selected="isSelected"
		:data-workspace-id="workspace.id"
	>
		<!-- Workspace Icon -->
		<div
			:class="[
				'flex items-center justify-center w-8 h-8 rounded-lg shrink-0',
				'bg-primary/10 text-primary'
			]"
		>
			<component :is="WorkspaceIcon" class="w-4 h-4" />
		</div>

		<!-- Workspace Info -->
		<div class="flex flex-col gap-0.5 flex-1 min-w-0">
			<div class="flex items-center gap-2">
				<span
					data-testid="workspace-name"
					:class="[
						'font-medium text-sm truncate',
						{ 'font-semibold': isSelected }
					]"
				>
					{{ workspace.name }}
				</span>
				<span
					v-if="workspace.isDefault"
					data-testid="default-badge"
					class="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-primary/10 text-primary"
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

		<!-- Right side: Keyboard shortcut or checkmark -->
		<div class="flex items-center gap-2 shrink-0">
			<kbd
				v-if="keyboardShortcut"
				:class="[
					'inline-flex items-center justify-center',
					'px-1.5 py-0.5 min-w-6',
					'text-[10px] font-mono font-medium',
					'bg-muted/50 text-muted-foreground',
					'border border-border/50 rounded',
					'shadow-sm'
				]"
			>
				{{ keyboardShortcut }}
			</kbd>
			<Check
				v-if="isSelected"
				data-testid="check-icon"
				class="h-4 w-4 text-primary"
			/>
		</div>
	</div>
</template>
