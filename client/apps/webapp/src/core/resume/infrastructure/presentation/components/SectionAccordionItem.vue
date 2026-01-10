<script setup lang="ts">
/**
 * SectionAccordionItem Component
 *
 * A vertical accordion-style section row for the Content Selection panel.
 * Design matches the Magic Patterns reference with:
 * - Purple checkbox on left
 * - Section name
 * - Counter badge (visible/total items)
 * - Chevron to expand/collapse
 */
import { Checkbox } from "@cvix/ui/components/ui/checkbox";
import {
	Tooltip,
	TooltipContent,
	TooltipProvider,
	TooltipTrigger,
} from "@cvix/ui/components/ui/tooltip";
import { ChevronRight } from "lucide-vue-next";
import { computed } from "vue";

interface Props {
	/** Section label text */
	label: string;

	/** Whether the section is enabled (checkbox checked) */
	enabled: boolean;

	/** Whether the section has data to display */
	hasData: boolean;

	/** Whether the accordion is expanded */
	expanded?: boolean;

	/** Number of visible items in the section */
	visibleCount?: number;

	/** Total number of items in the section */
	totalCount?: number;

	/** Tooltip to show when section is disabled */
	disabledTooltip?: string;
}

const props = withDefaults(defineProps<Props>(), {
	expanded: false,
	visibleCount: 0,
	totalCount: 0,
	disabledTooltip: "",
});

const emit = defineEmits<{
	toggle: [];
	expand: [];
}>();

const isDisabledDueToNoData = computed(() => !props.hasData);

const showItemCount = computed(
	() =>
		props.totalCount &&
		props.totalCount > 0 &&
		props.visibleCount !== undefined,
);

const counterText = computed(() => {
	if (!showItemCount.value) return null;
	return `${props.visibleCount}/${props.totalCount}`;
});

const handleCheckboxChange = () => {
	if (!isDisabledDueToNoData.value) {
		emit("toggle");
	}
};

const handleRowClick = (event: MouseEvent) => {
	// Don't expand if clicking on the checkbox
	const target = event.target as HTMLElement;
	if (target.closest("[data-slot='checkbox']")) {
		return;
	}
	if (!isDisabledDueToNoData.value && props.hasData) {
		emit("expand");
	}
};

const handleKeyDown = (event: KeyboardEvent) => {
	if (event.key === "Enter" || event.key === " ") {
		event.preventDefault();
		if (!isDisabledDueToNoData.value && props.hasData) {
			emit("expand");
		}
	}
};

const rowClasses = computed(() => {
	const base =
		"flex items-center gap-3 px-4 py-3 rounded-lg transition-colors duration-200";

	if (isDisabledDueToNoData.value) {
		return `${base} bg-muted/50 cursor-not-allowed opacity-60`;
	}

	return `${base} bg-secondary/50 hover:bg-secondary/80 cursor-pointer`;
});

const ariaLabel = computed(() => {
	let label = props.label;
	if (showItemCount.value) {
		label += ` (${props.visibleCount} of ${props.totalCount} items selected)`;
	}
	return `${label}, ${props.enabled ? "enabled" : "disabled"}`;
});
</script>

<template>
  <div class="w-full">
    <!-- Section Row -->
    <TooltipProvider v-if="isDisabledDueToNoData && disabledTooltip">
      <Tooltip>
        <TooltipTrigger as-child>
          <div
            :class="rowClasses"
            :aria-label="ariaLabel"
            :aria-disabled="true"
            role="button"
            tabindex="-1"
          >
            <Checkbox
              :checked="enabled"
              :disabled="true"
              class="shrink-0"
            />
            <span class="flex-1 text-sm font-medium text-muted-foreground truncate">
              {{ label }}
            </span>
            <span
              v-if="showItemCount"
              class="text-xs font-medium text-muted-foreground bg-muted px-2 py-0.5 rounded"
            >
              {{ counterText }}
            </span>
          </div>
        </TooltipTrigger>
        <TooltipContent side="top" align="center">
          {{ disabledTooltip }}
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>

    <div
      v-else
      :class="rowClasses"
      :aria-label="ariaLabel"
      :aria-expanded="expanded"
      role="button"
      tabindex="0"
      @click="handleRowClick"
      @keydown="handleKeyDown"
    >
      <!-- Checkbox -->
      <Checkbox
        :checked="enabled"
        class="shrink-0"
        @update:checked="handleCheckboxChange"
        @click.stop
      />

      <!-- Label -->
      <span
        :class="[
          'flex-1 text-sm font-medium truncate',
          enabled ? 'text-foreground' : 'text-muted-foreground'
        ]"
      >
        {{ label }}
      </span>

      <!-- Counter Badge -->
      <span
        v-if="showItemCount"
        :class="[
          'text-xs font-medium px-2 py-0.5 rounded',
          enabled
            ? 'text-primary-foreground bg-primary/80'
            : 'text-muted-foreground bg-muted'
        ]"
      >
        {{ counterText }}
      </span>

      <!-- Expand/Collapse Chevron -->
      <ChevronRight
        v-if="hasData"
        :class="[
          'h-4 w-4 shrink-0 transition-transform duration-200',
          expanded ? 'rotate-90' : '',
          enabled ? 'text-foreground' : 'text-muted-foreground'
        ]"
      />
    </div>

    <!-- Expanded Content Slot -->
    <div
      v-if="expanded && hasData"
      class="overflow-hidden"
    >
      <div class="pl-7 pr-4 py-2">
        <slot />
      </div>
    </div>
  </div>
</template>
