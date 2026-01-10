<script setup lang="ts">
/**
 * ItemToggleList Component
 *
 * Renders a list of toggleable items within an expanded section.
 * Each item shows:
 * - Checkbox for enable/disable
 * - Item label (title)
 * - Optional sublabel (organization, dates, etc.)
 *
 * Matches the Magic Patterns design with dark theme styling.
 */
import { Checkbox } from "@cvix/ui/components/ui/checkbox";
import { Label } from "@cvix/ui/components/ui/label";

export interface Item {
	/** Primary label for the item (e.g., job title, degree name) */
	label: string;
	/** Secondary label (e.g., company name, institution, dates) */
	sublabel?: string;
	/** Whether this item is enabled/visible */
	enabled: boolean;
}

interface Props {
	/** Items to display with toggle controls */
	items: Item[];
}

withDefaults(defineProps<Props>(), {});

const emit = defineEmits<{
	toggleItem: [index: number];
}>();

const handleToggle = (index: number) => {
	emit("toggleItem", index);
};
</script>

<template>
  <div v-if="items.length > 0" class="space-y-1">
    <div
      v-for="(item, index) in items"
      :key="index"
      :class="[
        'flex items-start gap-3 px-3 py-2.5 rounded-md transition-colors duration-150',
        'hover:bg-muted/50',
        item.enabled ? 'bg-transparent' : 'bg-muted/30'
      ]"
    >
      <Checkbox
        :id="`item-${index}`"
        :checked="item.enabled"
        class="mt-0.5 shrink-0"
        @update:checked="handleToggle(index)"
      />
      <div class="flex flex-col flex-1 min-w-0 gap-0.5">
        <Label
          :for="`item-${index}`"
          :class="[
            'text-sm font-medium cursor-pointer leading-tight',
            item.enabled ? 'text-foreground' : 'text-muted-foreground'
          ]"
        >
          {{ item.label }}
        </Label>
        <p
          v-if="item.sublabel"
          :class="[
            'text-xs leading-tight truncate',
            item.enabled ? 'text-muted-foreground' : 'text-muted-foreground/70'
          ]"
        >
          {{ item.sublabel }}
        </p>
      </div>
    </div>
  </div>
</template>
