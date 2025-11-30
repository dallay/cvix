<script setup lang="ts">
import { DateFormatter, getLocalTimeZone, today } from "@internationalized/date";
import { CalendarIcon } from "lucide-vue-next";
import { computed, useAttrs } from "vue";
import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { cn } from "@/lib/utils";

defineOptions({
  inheritAttrs: false,
});

const attrs = useAttrs();

const props = defineProps<{
  modelValue?: string;
  disabled?: boolean;
}>();

const emit = defineEmits(["update:modelValue"]);

const df = new DateFormatter("en-US", {
  dateStyle: "long",
});

const date = computed({
  get: () => (props.modelValue ? new Date(props.modelValue) : undefined),
  set: (value) => {
    if (value) {
      emit("update:modelValue", value.toISOString().split("T")[0]);
    }
  },
});

const defaultPlaceholder = today(getLocalTimeZone());
</script>

<template>
  <Popover>
    <PopoverTrigger as-child>
      <Button
        v-bind="attrs"
        variant="outline"
        :class="cn('w-full justify-start text-left font-normal', !date && 'text-muted-foreground')"
        :disabled="props.disabled"
        data-testid="date-picker-button"
      >
        <CalendarIcon class="mr-2 h-4 w-4" />
        {{ date ? df.format(date) : "Pick a date" }}
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-auto p-0">
      <Calendar
        v-model="date"
        :initial-focus="true"
        :default-placeholder="defaultPlaceholder"
        layout="month-and-year"
      />
    </PopoverContent>
  </Popover>
</template>