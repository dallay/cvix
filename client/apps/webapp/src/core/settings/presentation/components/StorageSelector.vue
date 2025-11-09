<script setup lang="ts">
import { Loader2 } from "lucide-vue-next";
import { computed, ref } from "vue";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import type { StorageType } from "@/core/resume/domain/ResumeStorage";
import {
	createResumeStorage,
	getStorageMetadata,
	type StorageMetadata,
} from "@/core/resume/infrastructure/storage";
import { useResumeStore } from "@/core/resume/infrastructure/store/resumeStore";
import { useStoragePreference } from "@/core/settings/presentation/composables";

const resumeStore = useResumeStore();
const {
	storagePreference,
	setStoragePreference,
	availableStorageTypes,
	isStorageAvailable,
} = useStoragePreference();

const storageOptions = computed<StorageMetadata[]>(() => {
	return getStorageMetadata().filter((meta) =>
		availableStorageTypes.value.includes(meta.type),
	);
});

const selectedStorage = ref<StorageType>(storagePreference.value);
const isMigrating = ref(false);
const migrationError = ref<string | null>(null);
const migrationSuccess = ref(false);

const hasChanges = computed(
	() => selectedStorage.value !== storagePreference.value,
);

async function applyStorageChange() {
	if (!hasChanges.value) return;

	isMigrating.value = true;
	migrationError.value = null;
	migrationSuccess.value = false;

	try {
		// Create new storage instance
		const newStorage = createResumeStorage(selectedStorage.value);

		// Check if we should migrate data
		const shouldMigrate = resumeStore.hasResume;

		// Change storage strategy in the store
		await resumeStore.changeStorageStrategy(newStorage, shouldMigrate);

		// Update the preference
		setStoragePreference(selectedStorage.value);

		migrationSuccess.value = true;

		// Clear success message after 3 seconds
		setTimeout(() => {
			migrationSuccess.value = false;
		}, 3000);
	} catch (error) {
		migrationError.value =
			error instanceof Error
				? error.message
				: "Failed to change storage. Please try again.";
	} finally {
		isMigrating.value = false;
	}
}

function cancelChanges() {
	selectedStorage.value = storagePreference.value;
}

function getStorageIcon(type: StorageType): string {
	const meta = storageOptions.value.find((opt) => opt.type === type);
	return meta?.icon ?? "💾";
}

function isPersistent(type: StorageType): boolean {
	const meta = storageOptions.value.find((opt) => opt.type === type);
	return meta?.persistence === "permanent";
}
</script>
<template>
  <Card>
    <CardHeader>
      <CardTitle>Storage Settings</CardTitle>
      <CardDescription>
        Choose where to save your resume data. You can change this at any time.
      </CardDescription>
    </CardHeader>
    <CardContent class="space-y-6">
      <!-- Storage Options -->
      <RadioGroup v-model="selectedStorage" :disabled="isMigrating">
        <div
          v-for="option in storageOptions"
          :key="option.type"
          class="flex items-start space-x-3 rounded-lg border p-4 transition-colors"
          :class="{
            'border-primary bg-primary/5': selectedStorage === option.type,
            'border-border hover:border-primary/50':
              selectedStorage !== option.type,
            'opacity-60': !isStorageAvailable(option.type),
          }"
        >
          <RadioGroupItem
            :id="option.type"
            :value="option.type"
            :disabled="!isStorageAvailable(option.type)"
          />
          <div class="flex-1">
            <Label
              :for="option.type"
              class="flex items-center gap-2 cursor-pointer"
            >
              <span class="text-2xl">{{ option.icon }}</span>
              <div>
                <div class="flex items-center gap-2">
                  <span class="font-semibold">{{ option.label }}</span>
                  <span
                    v-if="option.recommended"
                    class="text-xs px-2 py-0.5 rounded-full bg-primary text-primary-foreground"
                  >
                    Recommended
                  </span>
                  <span
                    v-if="isPersistent(option.type)"
                    class="text-xs px-2 py-0.5 rounded-full bg-green-500/10 text-green-700 dark:text-green-400"
                  >
                    Persistent
                  </span>
                </div>
                <p class="text-sm text-muted-foreground mt-1">
                  {{ option.description }}
                </p>
                <p class="text-xs text-muted-foreground mt-1">
                  Capacity: {{ option.capacity }}
                </p>
              </div>
            </Label>
          </div>
        </div>
      </RadioGroup>

      <!-- Current Storage Info -->
      <div
        v-if="!hasChanges"
        class="rounded-lg bg-muted/50 p-4 text-sm text-muted-foreground"
      >
        <div class="flex items-center gap-2">
          <span class="text-lg">{{ getStorageIcon(storagePreference) }}</span>
          <span>
            Currently using
            <strong class="text-foreground">{{
              storageOptions.find((opt) => opt.type === storagePreference)
                ?.label
            }}</strong>
          </span>
        </div>
      </div>

      <!-- Migration Status -->
      <Alert
        v-if="migrationSuccess"
        class="border-green-500/50 bg-green-500/10"
      >
        <AlertDescription class="text-green-700 dark:text-green-400">
          ✓ Storage changed successfully!
        </AlertDescription>
      </Alert>

      <Alert v-if="migrationError" variant="destructive">
        <AlertDescription>{{ migrationError }}</AlertDescription>
      </Alert>

      <!-- Action Buttons -->
      <div v-if="hasChanges" class="flex gap-3">
        <Button
          @click="applyStorageChange"
          :disabled="isMigrating"
          class="flex-1"
        >
          <Loader2 v-if="isMigrating" class="mr-2 h-4 w-4 animate-spin" />
          {{ isMigrating ? "Applying..." : "Apply Changes" }}
        </Button>
        <Button
          @click="cancelChanges"
          variant="outline"
          :disabled="isMigrating"
        >
          Cancel
        </Button>
      </div>
    </CardContent>
  </Card>
</template>
