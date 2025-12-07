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

const resumeSectionFilterService = new ResumeSectionFilterService();

/**
 * Section visibility store for managing which sections/items appear in PDF exports.
 * Handles state management, persistence, and reactive updates.
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
					itemCount =
						(Array.isArray(sectionData) ? sectionData.length : 0) ?? 0;
					hasData = itemCount > 0;
					visibleItemCount = resumeSectionFilterService.countVisibleItems(
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
			return resumeSectionFilterService.filterResume(
				resume.value,
				visibility.value,
			);
		});

		/**
		 * Initializes the store with a resume and loads saved preferences if available.
		 */
		function initialize(newResume: Resume, newResumeId?: string) {
			isLoading.value = true;
			error.value = null;

			try {
				resume.value = newResume;
				// Use provided resumeId or generate a temporary one
				const id = newResumeId || crypto.randomUUID();
				resumeId.value = id;

				// Try to load saved preferences
				const saved = sectionVisibilityStorage.load(id);
				if (saved) {
					visibility.value = saved;
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
			if (sectionVis.enabled) {
				// Disabling - set enabled to false
				sectionVis.enabled = false;
				sectionVis.expanded = false;
			} else {
				// Enabling - set enabled to true and enable all items
				sectionVis.enabled = true;
				sectionVis.items = sectionVis.items.map(() => true);
			}
		}

		/**
		 * Toggles the expanded state of a section.
		 */
		function toggleSectionExpanded(section: SectionType) {
			if (!visibility.value) return;

			if (section === "personalDetails") {
				visibility.value.personalDetails.expanded =
					!visibility.value.personalDetails.expanded;
			} else {
				const sectionVis = visibility.value[section] as ArraySectionVisibility;
				if (sectionVis.enabled) {
					sectionVis.expanded = !sectionVis.expanded;
				}
			}
		}

		/**
		 * Toggles an item's visibility within a section.
		 */
		function toggleItem(section: ArraySectionType, index: number) {
			if (!visibility.value) return;

			const sectionVis = visibility.value[section] as ArraySectionVisibility;
			if (index >= 0 && index < sectionVis.items.length) {
				sectionVis.items[index] = !sectionVis.items[index];

				// Auto-disable section if all items are disabled (FR-017)
				const hasVisibleItems = sectionVis.items.some((v: boolean) => v);
				if (!hasVisibleItems && sectionVis.enabled) {
					sectionVis.enabled = false;
					sectionVis.expanded = false;
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
			if (field === "location") {
				// Location is a nested object
				const location = fields.location;
				const currentState = Object.values(location).some((v) => v);
				Object.keys(location).forEach((key) => {
					location[key as keyof typeof location] = !currentState;
				});
			} else if (field === "profiles") {
				// Profiles are a map
				const currentState = Object.values(fields.profiles).some((v) => v);
				Object.keys(fields.profiles).forEach((key) => {
					fields.profiles[key] = !currentState;
				});
			} else if (field === "image") {
				fields.image = !fields.image;
			} else if (field === "email") {
				fields.email = !fields.email;
			} else if (field === "phone") {
				fields.phone = !fields.phone;
			} else if (field === "summary") {
				fields.summary = !fields.summary;
			} else if (field === "url") {
				fields.url = !fields.url;
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
		watch(
			() => visibility.value,
			(newVisibility) => {
				if (newVisibility) {
					sectionVisibilityStorage.save(newVisibility);
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

			// Computed
			sectionMetadata,
			filteredResume,

			// Actions
			initialize,
			toggleSection,
			toggleSectionExpanded,
			toggleItem,
			togglePersonalDetailsField,
			reset,
		};
	},
);
