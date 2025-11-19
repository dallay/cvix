<script setup lang="ts">
import { AlertCircle, CheckCircle2, ChevronRight } from "lucide-vue-next";
import { computed } from "vue";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
	Sheet,
	SheetContent,
	SheetDescription,
	SheetHeader,
	SheetTitle,
} from "@/components/ui/sheet";

import type {
	GroupedErrors,
	ValidationError,
} from "@/core/resume/infrastructure/presentation/composables/useJsonResume";

export interface ValidationErrorPanelProps {
	/**
	 * Whether the error panel is visible
	 */
	open: boolean;
	/**
	 * Validation errors to display
	 */
	errors: readonly ValidationError[];
}

const props = defineProps<ValidationErrorPanelProps>();

const emit = defineEmits<{
	/** Emitted when the panel is closed */
	"update:open": [value: boolean];
	/** Emitted when user clicks on an error to jump to that field */
	jumpTo: [section: string, path: string];
}>();

/**
 * Groups errors by section for organized display
 */
const groupedErrors = computed<GroupedErrors>(() => {
	return props.errors.reduce((acc, error) => {
		const section = error.section || "General";
		if (!acc[section]) {
			acc[section] = [];
		}
		acc[section].push(error);
		return acc;
	}, {} as GroupedErrors);
});

/**
 * Total count of errors
 */
const errorCount = computed(() => props.errors.length);

/**
 * Whether the resume is valid (no errors)
 */
const isValid = computed(() => errorCount.value === 0);

/**
 * Handles click on an error to jump to that field
 */
function handleJumpTo(section: string, path: string) {
	emit("jumpTo", section, path);
}

/**
 * Closes the panel
 */
function close() {
	emit("update:open", false);
}
</script>

<template>
  <Sheet :open="open" @update:open="(value) => emit('update:open', value)">
    <SheetContent side="bottom" class="h-[400px]">
      <SheetHeader>
        <SheetTitle>
          <div class="flex items-center gap-2">
            <CheckCircle2
                v-if="isValid"
                class="h-5 w-5 text-green-600 dark:text-green-400"
            />
            <AlertCircle
                v-else
                class="h-5 w-5 text-destructive"
            />
            <span>
							{{ isValid ? 'Validation Passed' : `${errorCount} Validation ${errorCount === 1 ? 'Error' : 'Errors'}` }}
						</span>
          </div>
        </SheetTitle>
        <SheetDescription>
          {{ isValid ? 'Your resume data is valid and ready to export.' : 'Please fix the errors below to ensure your resume is valid.' }}
        </SheetDescription>
      </SheetHeader>

      <div class="mt-6 overflow-y-auto max-h-[280px]">
        <Alert v-if="isValid" variant="default" class="border-green-200 dark:border-green-800">
          <CheckCircle2 class="h-4 w-4 text-green-600 dark:text-green-400"/>
          <AlertTitle>All Clear!</AlertTitle>
          <AlertDescription>
            Your resume passes all validation checks. You can safely export it as a JSON file.
          </AlertDescription>
        </Alert>

        <div v-else class="space-y-4">
          <div
              v-for="(errors, section) in groupedErrors"
              :key="section"
              class="rounded-lg border border-border p-4"
          >
            <h3 class="font-semibold text-sm text-foreground mb-2 flex items-center gap-2">
              <AlertCircle class="h-4 w-4 text-destructive"/>
              {{ section }}
              <span class="text-xs text-muted-foreground font-normal">
								({{ errors.length }} {{ errors.length === 1 ? 'error' : 'errors' }})
							</span>
            </h3>
            <ul class="space-y-2">
              <li
                  v-for="(error, index) in errors"
                  :key="`${section}-${index}`"
                  class="text-sm"
              >
                <Button
                    variant="ghost"
                    size="sm"
                    class="h-auto py-1 px-2 justify-start text-left w-full hover:bg-accent"
                    @click="handleJumpTo(String(section), error.path)"
                >
                  <ChevronRight class="h-4 w-4 mr-2 shrink-0"/>
                  <span class="text-destructive">{{ error.message }}</span>
                </Button>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div class="mt-4 flex justify-end">
        <Button variant="outline" @click="close">
          Close
        </Button>
      </div>
    </SheetContent>
  </Sheet>
</template>
