<script setup lang="ts">
import {
	Collapsible,
	CollapsibleContent,
} from "@cvix/ui/components/ui/collapsible";
import { useI18n } from "vue-i18n";

import type { Resume } from "@/core/resume/domain/Resume";
import type {
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
	(event: "toggle-item", section: SectionType, index: number): void;
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

const handleToggleItem = (section: SectionType, index: number) => {
	emit("toggle-item", section, index);
};
</script>

<template>
  <div class="space-y-4">
    <!-- Section Pills -->
    <div class="flex flex-wrap gap-2">
      <template v-for="section in metadata" :key="section.type">
        <!-- Personal Details - always shows as enabled, can expand to show fields -->
        <template v-if="section.type === 'personalDetails'">
          <Collapsible class="w-full" :open="visibility.personalDetails.expanded">
            <div class="flex gap-2 flex-wrap">
              <SectionTogglePill
                :label="t(section.labelKey)"
                :enabled="visibility.personalDetails.enabled"
                :has-data="section.hasData"
                :expanded="visibility.personalDetails.expanded"
                @toggle="handleToggleSection('personalDetails')"
                @expand="handleExpandSection('personalDetails')"
              />
            </div>
            <CollapsibleContent v-if="section.hasData" class="mt-3 ml-0">
              <ItemToggleList
                :items="getItemsForSection(section.type)"
                @toggle-item="(index) => handleToggleItem(section.type, index)"
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
						<div class="flex gap-2 flex-wrap items-center">
							<SectionTogglePill
								:label="t(section.labelKey)"
								:enabled="getArraySectionVisibility(section.type)?.enabled ?? false"
								:has-data="section.hasData"
								:expanded="getArraySectionVisibility(section.type)?.expanded"
								:visible-count="section.visibleItemCount"
								:total-count="section.itemCount"
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
								@toggle-item="(index) => handleToggleItem(section.type, index)"
							/>
						</CollapsibleContent>
					</Collapsible>
				</template>
      </template>
    </div>
  </div>
</template>
