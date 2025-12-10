<script setup lang="ts">
import { Checkbox } from "@cvix/ui/components/ui/checkbox";
import { Label } from "@cvix/ui/components/ui/label";

export interface Item {
	label: string;
	sublabel?: string;
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
  <div v-if="items.length > 0" class="space-y-3 py-2 px-4">
    <div v-for="(item, index) in items" :key="index" class="flex items-center gap-3">
      <Checkbox
        :id="`item-${index}`"
        :checked="item.enabled"
        @update:checked="handleToggle(index)"
      />
      <div class="flex flex-col flex-1 min-w-0">
        <Label
          :for="`item-${index}`"
          class="text-sm font-medium text-foreground cursor-pointer"
        >
          {{ item.label }}
        </Label>
        <p
          v-if="item.sublabel"
          class="text-xs text-muted-foreground mt-0.5 truncate"
        >
          {{ item.sublabel }}
        </p>
      </div>
    </div>
  </div>
</template>
