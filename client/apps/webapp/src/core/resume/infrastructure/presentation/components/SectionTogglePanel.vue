<script setup lang="ts">
/**
 * SectionTogglePanel Component
 *
 * Renders resume section toggle pills in a fixed, non-reorderable list.
 * Section order is determined by SECTION_TYPES and matches the backend template.
 *
 * ⚠️ SECTION ORDER PRESERVATION (FR-009, US4):
 * - Sections are rendered in the order provided by the metadata prop (derived from SECTION_TYPES)
 * - NO drag-and-drop affordances (draggable, handle icons, etc.)
 * - NO reorder controls (up/down arrows, move buttons, etc.)
 * - NO sortable/draggable libraries (VueDraggable, SortableJS, etc.)
 * - Order is locked to match backend template (engineering.stg)
 *
 * See: client/apps/webapp/src/core/resume/domain/SectionVisibility.ts (SECTION_TYPES)
 * See: specs/005-pdf-section-selector/plan.md (FR-009)
 */
import {
	Collapsible,
	CollapsibleContent,
} from "@cvix/ui/components/ui/collapsible";
import { useI18n } from "vue-i18n";

import type { Resume } from "@/core/resume/domain/Resume";
import type {
	ArraySectionType,
	ArraySectionVisibility,
	SectionMetadata,
	SectionType,
	SectionVisibility,
} from "@/core/resume/domain/SectionVisibility";

import ItemToggleList from "./ItemToggleList.vue";
import SectionTogglePill from "./SectionTogglePill.vue";

interface Props {
	/** The resume to derive section metadata from */
	resume: Resume;

	/** Current visibility preferences */
	visibility: SectionVisibility;

	/** Section metadata for rendering (in SECTION_TYPES order) */
	metadata: SectionMetadata[];
}

const props = defineProps<Props>();

const emit = defineEmits<{
	(event: "toggle-section", section: SectionType): void;
	(event: "expand-section", section: SectionType): void;
	(event: "toggle-item", section: ArraySectionType, index: number): void;
	(event: "toggle-field", field: string): void;
}>();

const { t } = useI18n();

/**
 * Prepares items for the ItemToggleList component.
 */
/**
 * Helper to safely access ArraySectionVisibility for a given section type.
 */
const getArraySectionVisibility = (
	section: SectionType,
): ArraySectionVisibility | null => {
	if (section === "personalDetails") return null;
	return props.visibility[
		section as keyof Omit<SectionVisibility, "personalDetails">
	] as ArraySectionVisibility;
};

const getItemsForSection = (section: SectionType) => {
	if (section === "personalDetails") {
		const fields = props.visibility.personalDetails.fields;
		return [
			{
				label: t("resume.basics.image"),
				enabled: fields.image,
			},
			{
				label: t("resume.basics.email"),
				enabled: fields.email,
			},
			{
				label: t("resume.basics.phone"),
				enabled: fields.phone,
			},
			{
				label: t("resume.basics.location"),
				sublabel: t("resume.basics.address"),
				enabled: fields.location.address,
			},
			{
				label: t("resume.basics.summary"),
				enabled: fields.summary,
			},
			{
				label: t("resume.basics.url"),
				enabled: fields.url,
			},
		];
	}

	// For array sections, get the items from the resume
	const sectionKey = section as keyof Resume;
	const items = props.resume[sectionKey];
	if (!items || !Array.isArray(items) || items.length === 0) {
		return [];
	}

	const vis = getArraySectionVisibility(section);
	if (!vis) return [];
	return items.map((item, index) => ({
		label:
			item.name ||
			item.position ||
			item.studyType ||
			item.title ||
			item.language ||
			item.interest ||
			`${section} #${index + 1}`,
		sublabel:
			item.company ||
			item.institution ||
			item.organization ||
			item.network ||
			item.level,
		enabled: vis.items[index] ?? true,
	}));
};

const handleToggleSection = (section: SectionType) => {
	emit("toggle-section", section);
};

const handleExpandSection = (section: SectionType) => {
	emit("expand-section", section);
};

const handleToggleItem = (section: ArraySectionType, index: number) => {
	emit("toggle-item", section, index);
};

/**
 * Maps personalDetails field indices to field names for emit.
 */
const personalDetailsFieldMap = [
	"image",
	"email",
	"phone",
	"location",
	"summary",
	"url",
] as const;

const handleTogglePersonalDetailsField = (index: number) => {
	const field = personalDetailsFieldMap[index];
	if (field) {
		emit("toggle-field", field);
	}
};

/**
 * Handles keyboard navigation for the section pills panel.
 * Supports arrow key navigation between pills.
 */
const onPanelKeydown = (event: KeyboardEvent) => {
	const target = event.target as HTMLElement;
	const pillButtons = target
		.closest("ul")
		?.querySelectorAll("button:not([disabled])");
	if (!pillButtons || pillButtons.length === 0) return;

	const currentIndex = Array.from(pillButtons).indexOf(
		document.activeElement as Element,
	);
	if (currentIndex === -1) return;

	let nextIndex: number;

	switch (event.key) {
		case "ArrowRight":
		case "ArrowDown":
			nextIndex = (currentIndex + 1) % pillButtons.length;
			event.preventDefault();
			break;
		case "ArrowLeft":
		case "ArrowUp":
			nextIndex = (currentIndex - 1 + pillButtons.length) % pillButtons.length;
			event.preventDefault();
			break;
		case "Home":
			nextIndex = 0;
			event.preventDefault();
			break;
		case "End":
			nextIndex = pillButtons.length - 1;
			event.preventDefault();
			break;
		default:
			return;
	}

	(pillButtons[nextIndex] as HTMLElement).focus();
};
</script>

<template>
	<div class="space-y-4">
		<!-- Section Pills -->
		<ul
			class="flex flex-wrap gap-2 list-none p-0 m-0"
			aria-label="Resume sections"
			@keydown="onPanelKeydown"
		>
			<template v-for="(section, idx) in metadata" :key="section.type">
				<!-- Personal Details - always shows as enabled, can expand to show fields -->
				<template v-if="section.type === 'personalDetails'">
					<Collapsible as="li" class="w-full" :open="visibility.personalDetails.expanded">
						<div class="flex gap-2 flex-wrap">
							<SectionTogglePill
								:label="t(section.labelKey)"
								:enabled="visibility.personalDetails.enabled"
								:has-data="section.hasData"
								:expanded="visibility.personalDetails.expanded"
								:aria-posinset="1"
								:aria-setsize="metadata.length"
								@toggle="handleToggleSection('personalDetails')"
								@expand="handleExpandSection('personalDetails')"
							/>
						</div>
						<CollapsibleContent v-if="section.hasData" class="mt-3 ml-0">
							<ItemToggleList
								:items="getItemsForSection(section.type)"
								@toggle-item="handleTogglePersonalDetailsField"
							/>
						</CollapsibleContent>
					</Collapsible>
				</template>

				<!-- Array Sections -->
				<template v-else>
					<Collapsible
						as="li"
						class="w-full"
						:open="getArraySectionVisibility(section.type)?.expanded"
					>
						<div class="flex gap-2 flex-wrap items-center">
							<SectionTogglePill
								:label="t(section.labelKey)"
								:enabled="getArraySectionVisibility(section.type)?.enabled ?? false"
								:has-data="section.hasData"
								:expanded="getArraySectionVisibility(section.type)?.expanded"
								:visible-count="section.visibleItemCount"
								:total-count="section.itemCount"
								:aria-posinset="idx + 1"
								:aria-setsize="metadata.length"
								:disabled-tooltip="
									!section.hasData ? t('resume.pdfPage.noDataAvailable') : undefined
								"
								@toggle="handleToggleSection(section.type)"
								@expand="handleExpandSection(section.type)"
							/>
						</div>
						<CollapsibleContent v-if="section.hasData" class="mt-3 ml-0">
							<ItemToggleList
								:items="getItemsForSection(section.type)"
								@toggle-item="(index) => handleToggleItem(section.type as ArraySectionType, index)"
							/>
						</CollapsibleContent>
					</Collapsible>
				</template>
			</template>
		</ul>
	</div>
</template>
