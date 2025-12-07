<script setup lang="ts">
import {
	Tooltip,
	TooltipContent,
	TooltipProvider,
	TooltipTrigger,
} from "@cvix/ui/components/ui/tooltip";
import { Check } from "lucide-vue-next";
import { computed } from "vue";

interface Props {
	label: string;

	enabled: boolean;

	hasData: boolean;

	expanded?: boolean;

	visibleCount?: number;

	totalCount?: number;

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

const ariaLabel = computed(() => {
	let label = props.label;
	if (props.visibleCount !== undefined && props.totalCount !== undefined) {
		label += ` (${props.visibleCount}/${props.totalCount})`;
	}
	return `${label}, ${props.enabled ? "enabled" : "disabled"}`;
});

const showItemCount = computed(
	() =>
		props.totalCount &&
		props.totalCount > 0 &&
		props.visibleCount !== undefined,
);

const buttonClasses = computed(() => {
	const base =
		"px-4 py-2 rounded-full font-medium transition-all duration-200 flex items-center gap-2 text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2";

	if (isDisabledDueToNoData.value) {
		return `${base} bg-muted text-muted-foreground cursor-not-allowed opacity-50`;
	}

	if (props.enabled) {
		return `${base} bg-primary text-primary-foreground shadow-md hover:shadow-lg active:shadow-md`;
	}

	return `${base} bg-background border border-border text-muted-foreground hover:bg-muted active:bg-background`;
});

const handleClick = () => {
	if (!isDisabledDueToNoData.value) {
		emit("toggle");
	}
};

const handleKeyDown = (e: KeyboardEvent) => {
	if (!isDisabledDueToNoData.value && (e.key === "Enter" || e.key === " ")) {
		e.preventDefault();
		emit("toggle");
	}
};
</script>

<template>
  <TooltipProvider v-if="isDisabledDueToNoData">
    <Tooltip>
      <TooltipTrigger as-child>
        <button
          :class="buttonClasses"
          :aria-label="ariaLabel"
          :aria-disabled="isDisabledDueToNoData"
          disabled
          type="button"
        >
          <span>{{ label }}</span>
          <span v-if="showItemCount" class="text-xs opacity-75">
            {{ visibleCount }}/{{ totalCount }}
          </span>
        </button>
      </TooltipTrigger>
      <TooltipContent
        v-if="disabledTooltip"
        side="top"
        align="center"
        :align-offset="0"
        :arrow-padding="0"
        :collision-padding="8"
        :collision-boundary="[]"
        avoid-collisions
        :hide-when-detached="false"
        position-strategy="absolute"
        update-position-strategy="optimized"
        sticky="partial"
      >
        {{ disabledTooltip }}
      </TooltipContent>
    </Tooltip>
  </TooltipProvider>

  <button
    v-else
    :class="buttonClasses"
    :aria-label="ariaLabel"
    :aria-pressed="enabled"
    :aria-expanded="expanded"
    type="button"
    @click="handleClick"
    @keydown="handleKeyDown"
  >
    <span v-if="enabled && !isDisabledDueToNoData" class="shrink-0">
      <span
        class="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center"
      >
        <Check class="w-3 h-3 text-primary-foreground" />
      </span>
    </span>
    <span>{{ label }}</span>
    <span v-if="showItemCount" class="text-xs opacity-75">
      {{ visibleCount }}/{{ totalCount }}
    </span>
  </button>
</template>
