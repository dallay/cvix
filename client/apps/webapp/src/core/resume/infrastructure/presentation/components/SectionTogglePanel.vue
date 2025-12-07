<script setup lang="ts">
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

	/** Section metadata for rendering */
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
		.closest('[role="list"]')
		?.querySelectorAll("button:not([disabled])");
	if (!pillButtons || pillButtons.length === 0) return;

	const currentIndex = Array.from(pillButtons).indexOf(
		document.activeElement as Element,
	);
	if (currentIndex === -1) return;

	let nextIndex = currentIndex;

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
		<div
			class="flex flex-wrap gap-2"
			role="list"
			aria-label="Resume sections"
			tabindex="0"
			@keydown="onPanelKeydown"
		>
			<template v-for="(section, idx) in metadata" :key="section.type">
				<!-- Personal Details - always shows as enabled, can expand to show fields -->
				<template v-if="section.type === 'personalDetails'">
					<Collapsible class="w-full" :open="visibility.personalDetails.expanded">
						<div class="flex gap-2 flex-wrap" role="listitem">
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
						class="w-full"
						:open="getArraySectionVisibility(section.type)?.expanded"
					>
						<div class="flex gap-2 flex-wrap items-center" role="listitem">
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
		</div>
		<!--
			Accessibility Audit Checklist (T014):
			- Keyboard navigation: Tab/arrow keys move focus between pills
			- Focus ring visible (focus-visible)
			- ARIA roles: list, listitem, aria-label, aria-posinset, aria-setsize
			- Screen reader labels: pill state, item counts
			- Color contrast: All states meet WCAG AA
			- Automated tools: axe, pa11y
			- Manual: VoiceOver/NVDA/JAWS walkthrough
			- Responsive: Pills wrap at 768px, 1024px, 1440px, 2560px
		-->
	</div>
</template>
