import { useDebounceFn } from "@vueuse/core";
import { defineStore } from "pinia";
import { computed, ref, watch } from "vue";
import { ResumeSectionFilterService } from "@/core/resume/application/ResumeSectionFilterService";
import type { Resume } from "@/core/resume/domain/Resume";
import type {
	ArraySectionType,
	ArraySectionVisibility,
	PersonalDetailsVisibility,
	SectionMetadata,
	SectionType,
	SectionVisibility,
} from "@/core/resume/domain/SectionVisibility";
import {
	createDefaultVisibility,
	SECTION_TYPES,
} from "@/core/resume/domain/SectionVisibility";
import { sectionVisibilityStorage } from "@/core/resume/infrastructure/storage/SectionVisibilityStorage";

/**
 * Default resume ID for single-resume mode.
 * This ensures localStorage persistence works across page reloads.
 */
const DEFAULT_RESUME_ID = "default";

// Lazy-initialized filter service to improve testability (can be mocked by tests)
let resumeSectionFilterService: ResumeSectionFilterService | null = null;
function getFilterService(): ResumeSectionFilterService {
	resumeSectionFilterService ??= new ResumeSectionFilterService();
	return resumeSectionFilterService;
}

/**
 * Section visibility store for managing which sections/items appear in PDF exports.
 * Handles state management, persistence, and reactive updates.
 *
 * ARCHITECTURE NOTE: Expanded state is deliberately SEPARATE from visibility state.
 * - `visibility` tracks what's visible in the PDF (triggers PDF regeneration)
 * - `expandedSections` tracks UI accordion state (does NOT trigger PDF regeneration)
 * This separation prevents PDF re-renders when users expand/collapse sections.
 */
export const useSectionVisibilityStore = defineStore(
	"section-visibility",
	() => {
		// State
		const visibility = ref<SectionVisibility | null>(null);
		const resume = ref<Resume | null>(null);
		const resumeId = ref<string>("");
		const isLoading = ref(false);
		const error = ref<Error | null>(null);

		/**
		 * UI-only state: tracks which sections are expanded in the accordion.
		 * Separated from visibility to avoid PDF re-renders when expanding sections.
		 * Key: section type, Value: expanded state
		 */
		const expandedSections = ref<Record<SectionType, boolean>>({
			personalDetails: false,
			work: false,
			volunteer: false,
			education: false,
			awards: false,
			certificates: false,
			publications: false,
			skills: false,
			languages: false,
			interests: false,
			references: false,
			projects: false,
		});

		/**
		 * Syncs expanded state from loaded visibility (legacy support).
		 * Migrates expanded state from visibility object to separate ref.
		 */
		function syncExpandedStateFromVisibility(vis: SectionVisibility) {
			const newExpanded = { ...expandedSections.value };
			for (const sectionType of SECTION_TYPES) {
				if (sectionType === "personalDetails") {
					newExpanded.personalDetails = vis.personalDetails.expanded;
				} else {
					const sectionVis = vis[sectionType] as ArraySectionVisibility;
					newExpanded[sectionType] = sectionVis.expanded;
				}
			}
			expandedSections.value = newExpanded;
		}

		/**
		 * Gets the expanded state for a section.
		 */
		function isSectionExpanded(section: SectionType): boolean {
			return expandedSections.value[section] ?? false;
		}

		/**
		 * Gets metadata for all sections (for rendering pills).
		 */
		const sectionMetadata = computed(() => {
			if (!visibility.value || !resume.value) {
				return [];
			}

			const metadata: SectionMetadata[] = [];

			for (const sectionType of SECTION_TYPES) {
				const vis = visibility.value[
					sectionType as keyof typeof visibility.value
				] as ArraySectionVisibility | PersonalDetailsVisibility;
				let hasData = false;
				let itemCount = 0;
				let visibleItemCount = 0;

				if (sectionType === "personalDetails") {
					// Personal Details always has data
					hasData = true;
				} else {
					// Array sections
					const sectionData = resume.value[sectionType as keyof Resume];
					itemCount = Array.isArray(sectionData) ? sectionData.length : 0;
					hasData = itemCount > 0;
					visibleItemCount = getFilterService().countVisibleItems(
						vis as ArraySectionVisibility,
					);
				}

				metadata.push({
					type: sectionType,
					labelKey: `resume.sections.${sectionType}`,
					hasData,
					itemCount,
					visibleItemCount,
				});
			}

			return metadata;
		});

		/**
		 * Gets the filtered resume based on current visibility preferences.
		 */
		const filteredResume = computed(() => {
			if (!visibility.value || !resume.value) {
				return null;
			}
			return getFilterService().filterResume(resume.value, visibility.value);
		});

		/**
		 * Initializes the store with a resume and loads saved preferences if available.
		 * Uses a stable default ID for single-resume mode to ensure localStorage persistence works.
		 */
		function initialize(newResume: Resume, newResumeId?: string) {
			isLoading.value = true;
			error.value = null;

			try {
				resume.value = newResume;
				// Use provided resumeId or fallback to stable default for single-resume mode
				const id = newResumeId || DEFAULT_RESUME_ID;
				resumeId.value = id;

				// Try to load saved preferences
				const saved = sectionVisibilityStorage.load(id);
				if (saved) {
					syncVisibilityWithResume(saved, newResume);
					visibility.value = saved;
					// Restore expanded state from saved visibility (legacy support)
					syncExpandedStateFromVisibility(saved);
				} else {
					// Create defaults if no saved preferences
					visibility.value = createDefaultVisibility(id, newResume);
				}
			} catch (err) {
				error.value =
					err instanceof Error ? err : new Error("Failed to initialize");
				visibility.value = null;
			} finally {
				isLoading.value = false;
			}
		}

		/**
		 * Toggles a section's enabled state.
		 */
		function toggleSection(section: SectionType) {
			if (!visibility.value) return;

			if (section === "personalDetails") {
				// Personal Details cannot be disabled (FR-007)
				return;
			}

			const sectionVis = visibility.value[section] as ArraySectionVisibility;

			// Determine current state based on items
			const allSelected =
				sectionVis.items.length > 0 && sectionVis.items.every(Boolean);
			const isEnabled = sectionVis.enabled;

			// Logic:
			// If Enabled AND All Selected -> Uncheck (Clear All)
			// If Enabled AND Not All Selected (Indeterminate) -> Check (Select All)
			// If Disabled -> Check (Select All)

			if (isEnabled && allSelected) {
				// Full -> Empty
				sectionVis.enabled = false;
				// Collapse section when disabling (UI state)
				expandedSections.value = {
					...expandedSections.value,
					[section]: false,
				};
				// Clear child items state for consistency
				sectionVis.items = sectionVis.items.map(() => false);
			} else {
				// Indeterminate/Empty -> Full
				sectionVis.enabled = true;
				sectionVis.items = sectionVis.items.map(() => true);
			}
		}

		/**
		 * Toggles the expanded state of a section (UI-only, does NOT trigger PDF regeneration).
		 */
		function toggleSectionExpanded(section: SectionType) {
			// For array sections, only allow expansion if enabled
			if (section !== "personalDetails" && visibility.value) {
				const sectionVis = visibility.value[section] as ArraySectionVisibility;
				if (!sectionVis.enabled) {
					return;
				}
			}

			expandedSections.value = {
				...expandedSections.value,
				[section]: !expandedSections.value[section],
			};
		}

		/**
		 * Toggles an item's visibility within a section.
		 */
		function toggleItem(section: ArraySectionType, index: number) {
			if (!visibility.value) return;

			const sectionVis = visibility.value[section] as ArraySectionVisibility;
			if (index >= 0 && index < sectionVis.items.length) {
				sectionVis.items[index] = !sectionVis.items[index];

				// Update parent enabled state based on children
				// "Parent state must be derived from its children"
				// If any item is visible -> section must be enabled
				// If no items are visible -> section must be disabled
				const hasVisibleItems = sectionVis.items.some(Boolean);
				sectionVis.enabled = hasVisibleItems;

				if (!hasVisibleItems) {
					// Collapse section when all items are disabled (UI state)
					expandedSections.value = {
						...expandedSections.value,
						[section]: false,
					};
				}
			}
		}

		/**
		 * Toggles a Personal Details field visibility.
		 */
		function togglePersonalDetailsField(field: string) {
			if (!visibility.value) return;
			if (field === "name") return; // Name always visible (FR-013)

			const fields = visibility.value.personalDetails.fields;

			// Handle simple boolean fields
			const simpleFields = [
				"image",
				"email",
				"phone",
				"summary",
				"url",
			] as const;
			if (simpleFields.includes(field as (typeof simpleFields)[number])) {
				const key = field as (typeof simpleFields)[number];
				fields[key] = !fields[key];
				return;
			}

			if (field === "location") {
				// Location is a nested object
				const location = fields.location;
				const currentState = Object.values(location).some(Boolean);
				Object.keys(location).forEach((key) => {
					location[key as keyof typeof location] = !currentState;
				});
			} else if (field === "profiles") {
				// Profiles are a map
				const currentState = Object.values(fields.profiles).some(Boolean);
				Object.keys(fields.profiles).forEach((key) => {
					fields.profiles[key] = !currentState;
				});
			} else if (field.startsWith("profiles:")) {
				// Individual profile toggle
				const network = field.split(":")[1];
				if (network) {
					if (typeof fields.profiles[network] === "boolean") {
						fields.profiles[network] = !fields.profiles[network];
					} else {
						// Initialize if missing
						fields.profiles[network] = false;
					}
				}
			}
		}

		/**
		 * Synchronizes visibility state with the current resume.
		 * Ensures arrays match in length and new items are initialized.
		 */
		function syncVisibilityWithResume(
			vis: SectionVisibility,
			currentResume: Resume,
		) {
			// Sync Array Sections
			for (const section of SECTION_TYPES) {
				if (section === "personalDetails") continue;

				const sectionVis = vis[section] as ArraySectionVisibility;
				const resumeItems = currentResume[section as keyof Resume] as unknown[];
				const currentCount = sectionVis.items.length;
				const targetCount = Array.isArray(resumeItems) ? resumeItems.length : 0;

				if (currentCount < targetCount) {
					// Add new items (enabled by default)
					const newItems = Array.from(
						{ length: targetCount - currentCount },
						() => true,
					);
					sectionVis.items.push(...newItems);
				} else if (currentCount > targetCount) {
					// Trim removed items
					sectionVis.items.splice(targetCount);
				}
			}

			// Sync Profiles
			if (currentResume.basics?.profiles) {
				currentResume.basics.profiles.forEach((profile) => {
					if (
						vis.personalDetails.fields.profiles[profile.network] === undefined
					) {
						vis.personalDetails.fields.profiles[profile.network] = true;
					}
				});
			}
		}

		/**
		 * Resets all preferences to defaults.
		 */
		function reset() {
			if (!resume.value) return;
			visibility.value = createDefaultVisibility(resumeId.value, resume.value);
		}

		/**
		 * Watch for changes and persist to storage.
		 */
		const debouncedSave = useDebounceFn((vis: SectionVisibility) => {
			sectionVisibilityStorage.save(vis);
		}, 300);

		watch(
			() => visibility.value,
			(newVisibility) => {
				if (newVisibility) {
					debouncedSave(newVisibility).catch((e) => {
						console.error("Failed to save section visibility:", e);
					});
				}
			},
			{ deep: true },
		);

		return {
			// State
			visibility,
			resume,
			isLoading,
			error,
			expandedSections,

			// Computed
			sectionMetadata,
			filteredResume,

			// Actions
			initialize,
			toggleSection,
			toggleSectionExpanded,
			isSectionExpanded,
			toggleItem,
			togglePersonalDetailsField,
			reset,
		};
	},
);
