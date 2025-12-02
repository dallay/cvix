<script setup lang="ts">
import type { DateValue } from "@internationalized/date"
import { CalendarIcon } from "lucide-vue-next"
import type { HTMLAttributes } from "vue"
import { computed } from "vue"
import { CalendarDate, DateFormatter, getLocalTimeZone } from "@internationalized/date"
import { Calendar } from "../calendar"
import { Button } from "../button"
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from "../popover"
import { cn } from "../../../lib/utils"

const props = withDefaults(
	defineProps<{
		modelValue?: string | DateValue
		placeholder?: string
		disabled?: boolean
		class?: HTMLAttributes["class"]
		id?: string
	}>(),
	{
		placeholder: "Pick a date",
	},
)

const emits = defineEmits<{
	"update:modelValue": [value: string | undefined]
}>()

const df = new DateFormatter("en-US", {
	dateStyle: "long",
})

const value = computed({
	get: () => {
		if (!props.modelValue) return undefined
		
		// If it's already a DateValue object, return it
		if (typeof props.modelValue === "object" && "calendar" in props.modelValue) {
			return props.modelValue as DateValue
		}
		
		// Parse string date to DateValue
		if (typeof props.modelValue === "string" && props.modelValue) {
			try {
				const parts = props.modelValue.split("-")
				if (parts.length !== 3) return undefined
				
				const year = Number.parseInt(parts[0] ?? "", 10)
				const month = Number.parseInt(parts[1] ?? "", 10)
				const day = Number.parseInt(parts[2] ?? "", 10)
				
				// Validate date components are valid numbers and within reasonable ranges
				if (
					!Number.isNaN(year) &&
					!Number.isNaN(month) &&
					!Number.isNaN(day) &&
					year > 0 &&
					month >= 1 &&
					month <= 12 &&
					day >= 1 &&
					day <= 31
				) {
					// CalendarDate constructor validates day is valid for the given month/year
					return new CalendarDate(year, month, day)
				}
			} catch (e) {
				console.error(`Error parsing date value "${props.modelValue}":`, e)
			}
		}
		
		return undefined
	},
	set: (val) => {
		if (!val) {
			emits("update:modelValue", undefined)
			return
		}
		
		// Convert DateValue to string format (YYYY-MM-DD)
		const year = val.year
		const month = String(val.month).padStart(2, "0")
		const day = String(val.day).padStart(2, "0")
		emits("update:modelValue", `${year}-${month}-${day}`)
	},
})

const displayValue = computed(() => {
	if (value.value) {
		return df.format(value.value.toDate(getLocalTimeZone()))
	}
	return props.placeholder
})
</script>

<template>
	<Popover>
		<PopoverTrigger as-child>
			<Button
				variant="outline"
				:class="
					cn(
						'w-full justify-start text-left font-normal',
						!value && 'text-muted-foreground',
						props.class,
					)
				"
				:disabled="disabled"
			>
				<CalendarIcon class="mr-2 h-4 w-4" />
				{{ displayValue }}
			</Button>
		</PopoverTrigger>
		<PopoverContent class="w-auto p-0">
			<Calendar v-model="value" initial-focus />
		</PopoverContent>
	</Popover>
</template>
