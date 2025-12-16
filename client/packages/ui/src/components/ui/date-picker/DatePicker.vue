<script setup lang="ts">
import type { DateValue } from "reka-ui"
import { CalendarIcon } from "lucide-vue-next"
import type { HTMLAttributes } from "vue"
import { computed, ref, watch } from "vue"
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
		required?: boolean
		name?: string
		class?: HTMLAttributes["class"]
		id?: string
		/** Locale for date formatting and calendar display (e.g., "en-US", "es-ES"). Falls back to navigator.language or "en-US". */
		locale?: string
	}>(),
	{
		placeholder: "Pick a date",
	},
)

const emit = defineEmits<{
	(e: 'update:modelValue', value: string | undefined): void
	(e: 'validation-error', payload: { value: string | undefined, message: string }): void
}>()

const resolvedLocale = computed(() => {
	return props.locale || (typeof navigator !== "undefined" && navigator.language) || "en-US"
})

const df = computed(() => new DateFormatter(resolvedLocale.value, {
	dateStyle: "long",
}))

const errorMessage = ref<string | null>(null)
const value = computed<DateValue | undefined>({
	get: () => {
		if (!props.modelValue) {
			errorMessage.value = null
			return undefined
		}

		// If it's already a DateValue object, return it
		if (typeof props.modelValue === "object" && "calendar" in props.modelValue) {
			errorMessage.value = null
			return props.modelValue as DateValue
		}

		// Parse string date to DateValue
		if (typeof props.modelValue === "string" && props.modelValue) {
			try {
				const parts = props.modelValue.split("-")
				if (parts.length !== 3) {
					const message = "Invalid date format"
					errorMessage.value = message
					emit("validation-error", { value: props.modelValue, message })
					return undefined
				}

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
					try {
						// CalendarDate constructor validates day is valid for the given month/year
						const date = new CalendarDate(year, month, day) as unknown as DateValue
						errorMessage.value = null
						return date
					} catch (e: any) {
						const message = e instanceof Error ? e.message : "Invalid date"
						errorMessage.value = message
						emit("validation-error", { value: props.modelValue, message })
						// eslint-disable-next-line no-console
						console.warn("Invalid date:", props.modelValue, e)
						return undefined
					}
				} else {
					const message = "Date components out of range"
					errorMessage.value = message
					emit("validation-error", { value: props.modelValue, message })
					return undefined
				}
			} catch (e: any) {
				const message = e instanceof Error ? e.message : "Error parsing date"
				errorMessage.value = message
				emit("validation-error", { value: props.modelValue, message })
				// eslint-disable-next-line no-console
				console.error(`Error parsing date value "${props.modelValue}":`, e)
				return undefined
			}
		}

		errorMessage.value = null
		return undefined
	},
	set: (val) => {
		if (!val) {
			errorMessage.value = null
			emit("update:modelValue", undefined)
			return
		}

		// Convert DateValue to string format (YYYY-MM-DD)
		const year = val.year
		const month = String(val.month).padStart(2, "0")
		const day = String(val.day).padStart(2, "0")
		errorMessage.value = null
		emit("update:modelValue", `${year}-${month}-${day}`)
	},
})

// Watch for modelValue changes to clear error if value becomes valid
watch(
	() => props.modelValue,
	() => {
		if (value.value) {
			errorMessage.value = null
		}
	}
)

defineExpose({ errorMessage })

const displayValue = computed(() => {
	if (value.value) {
		return df.value.format(value.value.toDate(getLocalTimeZone()))
	}
	return props.placeholder
})
</script>

<template>
	<Popover>
		<!-- Hidden input to participate in native form validation -->
		<input
			v-if="props.id || props.name"
			:type="'text'"
			:id="props.id"
			:name="props.name"
			:value="value ? `${value.year}-${String(value.month).padStart(2, '0')}-${String(value.day).padStart(2, '0')}` : ''"
			:required="props.required"
			class="sr-only absolute opacity-0 pointer-events-none h-0 w-0 p-0 m-0"
			aria-hidden="true"
			readonly
		/>
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
				:aria-required="props.required ? 'true' : undefined"
			>
				<CalendarIcon class="mr-2 h-4 w-4" />
				{{ displayValue }}
			</Button>
		</PopoverTrigger>
		<PopoverContent class="w-auto p-0">
			<Calendar v-model="value" :locale="resolvedLocale" initial-focus />
		</PopoverContent>
	</Popover>
</template>
