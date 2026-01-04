<script setup lang="ts">
import { Alert, AlertDescription } from "@cvix/ui/components/ui/alert";
import {
	AlertDialog,
	AlertDialogAction,
	AlertDialogCancel,
	AlertDialogContent,
	AlertDialogDescription,
	AlertDialogFooter,
	AlertDialogHeader,
	AlertDialogTitle,
} from "@cvix/ui/components/ui/alert-dialog";
import { Button } from "@cvix/ui/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@cvix/ui/components/ui/card";
import { Label } from "@cvix/ui/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@cvix/ui/components/ui/radio-group";
import {
	AlertTriangle,
	Cloud,
	Database,
	HardDrive,
	Loader2,
	Lock,
	type LucideIcon,
} from "lucide-vue-next";
import { computed, ref } from "vue";
import type { StorageType } from "@/core/resume/domain/ResumeStorage";
import {
	createResumeStorage,
	getStorageMetadata,
	type StorageMetadata,
} from "@/core/resume/infrastructure/storage";
import { useResumeStore } from "@/core/resume/infrastructure/store/resume.store.ts";
import { useStoragePreference } from "@/core/settings/infrastructure/presentation/composables";

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

// Confirmation dialog state
const showConfirmDialog = ref(false);
const isCheckingOldStorage = ref(false);
const hasDataInOldStorage = ref(false);
const oldStorageLabel = ref<string>("");
const newStorageLabel = ref<string>("");

const hasChanges = computed(
	() => selectedStorage.value !== storagePreference.value,
);

// Map icon names to Lucide components
const iconMap: Record<string, LucideIcon> = {
	lock: Lock,
	"hard-drive": HardDrive,
	database: Database,
	cloud: Cloud,
};

/**
 * Check if the current storage has data before switching.
 * This prevents silent data loss when switching storage types.
 */
async function checkOldStorageForData(): Promise<boolean> {
	try {
		isCheckingOldStorage.value = true;
		const oldStorage = createResumeStorage(storagePreference.value);
		const result = await oldStorage.load();
		return result.data !== null;
	} catch (error) {
		console.warn("[StorageSelector] Failed to check old storage", {
			storagePreference: storagePreference.value,
			error,
		});
		// Assume there might be data if we can't check
		return true;
	} finally {
		isCheckingOldStorage.value = false;
	}
}

/**
 * Initiates the storage change process.
 * Shows a confirmation dialog if data exists in the old storage.
 */
async function applyStorageChange() {
	if (!hasChanges.value) return;

	// Check if old storage has data (don't rely on in-memory state)
	hasDataInOldStorage.value = await checkOldStorageForData();

	if (hasDataInOldStorage.value) {
		// Show confirmation dialog
		const oldMeta = storageOptions.value.find(
			(opt) => opt.type === storagePreference.value,
		);
		const newMeta = storageOptions.value.find(
			(opt) => opt.type === selectedStorage.value,
		);

		oldStorageLabel.value = oldMeta?.label ?? storagePreference.value;
		newStorageLabel.value = newMeta?.label ?? selectedStorage.value;

		showConfirmDialog.value = true;
	} else {
		// No data to migrate, proceed directly
		await performStorageChange(false);
	}
}

/**
 * Performs the actual storage change with optional data migration.
 */
async function performStorageChange(migrateData: boolean) {
	isMigrating.value = true;
	migrationError.value = null;
	migrationSuccess.value = false;

	try {
		const oldStorage = createResumeStorage(storagePreference.value);
		const newStorage = createResumeStorage(selectedStorage.value);

		if (migrateData) {
			// Load data from old storage
			const oldData = await oldStorage.load();

			if (oldData.data) {
				// Save to new storage
				await newStorage.save(oldData.data);

				// Update store with migrated data
				resumeStore.setResume(oldData.data);
			}
		}

		// Change storage strategy in the store
		await resumeStore.changeStorageStrategy(newStorage, false);

		// Update the preference
		await setStoragePreference(selectedStorage.value);

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
		showConfirmDialog.value = false;
	}
}

function handleConfirmMigration() {
	performStorageChange(true);
}

function handleConfirmNoMigration() {
	performStorageChange(false);
}

function handleCancelChange() {
	showConfirmDialog.value = false;
	selectedStorage.value = storagePreference.value;
}

function cancelChanges() {
	selectedStorage.value = storagePreference.value;
}

function getStorageIconComponent(iconName: string): LucideIcon {
	return iconMap[iconName] ?? HardDrive;
}

function isPersistent(type: StorageType): boolean {
	const meta = storageOptions.value.find((opt) => opt.type === type);
	return meta?.persistence === "permanent";
}

/**
 * Exposed component refs for testing purposes.
 *
 * @internal These properties are exposed solely for unit testing and should not be
 * relied upon in production code. The component's public API is defined by its props
 * and emitted events.
 */
defineExpose({
	selectedStorage,
	showConfirmDialog,
	isMigrating,
	hasDataInOldStorage,
	migrationError,
	migrationSuccess,
});
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
          class="flex items-start space-x-3 rounded-lg border p-3 sm:p-4 transition-colors"
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
            class="mt-0.5"
          />
          <div class="flex-1 min-w-0">
            <Label
              :for="option.type"
              class="flex flex-col sm:flex-row sm:items-center gap-2 cursor-pointer"
            >
              <div class="flex items-center gap-2">
                <component
                  :is="getStorageIconComponent(option.icon)"
                  class="h-5 w-5 shrink-0 text-muted-foreground"
                />
                <span class="font-semibold text-sm sm:text-base break-words">{{
                  option.label
                }}</span>
              </div>
              <div class="flex flex-wrap items-center gap-1.5 sm:gap-2">
                <span
                  v-if="option.recommended"
                  class="text-xs px-2 py-0.5 rounded-full bg-primary text-primary-foreground whitespace-nowrap"
                >
                  Recommended
                </span>
                <span
                  v-if="isPersistent(option.type)"
                  class="text-xs px-2 py-0.5 rounded-full bg-success/10 text-success whitespace-nowrap"
                >
                  Persistent
                </span>
              </div>
            </Label>
            <p class="text-xs sm:text-sm text-muted-foreground mt-1.5 sm:mt-2 break-words">
              {{ option.description }}
            </p>
            <p class="text-xs text-muted-foreground mt-1">
              Capacity: {{ option.capacity }}
            </p>
          </div>
        </div>
      </RadioGroup>

      <!-- Current Storage Info -->
      <div
        v-if="!hasChanges"
        class="rounded-lg bg-muted/50 p-3 sm:p-4 text-xs sm:text-sm text-muted-foreground"
      >
        <div class="flex items-center gap-2">
          <component
            :is="
              getStorageIconComponent(
                storageOptions.find((opt) => opt.type === storagePreference)
                  ?.icon ?? 'hard-drive',
              )
            "
            class="h-4 w-4 sm:h-5 sm:w-5 shrink-0"
          />
          <span class="break-words">
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
        <AlertDescription class="text-green-700 dark:text-green-400 text-sm">
          ✓ Storage changed successfully!
        </AlertDescription>
      </Alert>

      <Alert v-if="migrationError" variant="destructive">
        <AlertDescription class="text-sm break-words">{{ migrationError }}</AlertDescription>
      </Alert>

      <!-- Action Buttons -->
      <div v-if="hasChanges" class="flex flex-col sm:flex-row gap-2 sm:gap-3">
        <Button
          @click="applyStorageChange"
          :disabled="isMigrating || isCheckingOldStorage"
          class="w-full sm:flex-1"
        >
          <Loader2 v-if="isMigrating || isCheckingOldStorage" class="mr-2 h-4 w-4 animate-spin" />
          {{ isMigrating ? "Applying..." : isCheckingOldStorage ? "Checking..." : "Apply Changes" }}
        </Button>
        <Button
          @click="cancelChanges"
          variant="outline"
          :disabled="isMigrating || isCheckingOldStorage"
          class="w-full sm:w-auto"
        >
          Cancel
        </Button>
      </div>
    </CardContent>
  </Card>

  <!-- Confirmation Dialog -->
  <AlertDialog :open="showConfirmDialog" @update:open="showConfirmDialog = $event">
    <AlertDialogContent>
      <AlertDialogHeader>
        <AlertDialogTitle class="flex items-center gap-2">
          <AlertTriangle class="h-5 w-5 text-warning" />
          Switch Storage Location?
        </AlertDialogTitle>
        <AlertDialogDescription class="space-y-3 pt-2">
          <p>
            You have resume data stored in <strong>{{ oldStorageLabel }}</strong>.
          </p>
          <p>
            Switching to <strong>{{ newStorageLabel }}</strong> will make your current data
            inaccessible unless you migrate it.
          </p>
          <div class="rounded-lg bg-muted p-3 text-sm space-y-2">
            <p class="font-semibold text-foreground">Your options:</p>
            <ul class="list-disc list-inside space-y-1 text-muted-foreground">
              <li>
                <strong class="text-foreground">Migrate:</strong> Copy your resume data to {{ newStorageLabel }}
              </li>
              <li>
                <strong class="text-foreground">Don't migrate:</strong> Switch without copying (you can switch back later to access your data)
              </li>
            </ul>
          </div>
        </AlertDialogDescription>
      </AlertDialogHeader>
      <AlertDialogFooter class="flex-col sm:flex-row gap-2">
        <AlertDialogCancel @click="handleCancelChange" :disabled="isMigrating">
          Cancel
        </AlertDialogCancel>
        <Button
          @click="handleConfirmNoMigration"
          variant="outline"
          :disabled="isMigrating"
        >
          Switch Without Migrating
        </Button>
        <AlertDialogAction @click="handleConfirmMigration" :disabled="isMigrating">
          <Loader2 v-if="isMigrating" class="mr-2 h-4 w-4 animate-spin" />
          {{ isMigrating ? "Migrating..." : "Migrate & Switch" }}
        </AlertDialogAction>
      </AlertDialogFooter>
    </AlertDialogContent>
  </AlertDialog>
</template>
