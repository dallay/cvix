<script setup lang="ts">
// Format location for display (handles string or object)
const formattedLocation = computed(() => {
	const loc = debouncedData.value.personalInfo?.location;
	if (!loc) return null;
	if (typeof loc === "string") return loc;
	if (typeof loc === "object") {
		// JSON Resume: { city, region, countryCode, ... }
		const { city, region, countryCode } = loc;
		return [city, region, countryCode].filter(Boolean).join(", ");
	}
	return null;
});

import { computed, onBeforeUnmount, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { Card, CardContent } from "@/components/ui/card";
import type { ResumeData } from "../types/resume.ts";
import { formatPeriod as formatPeriodUtil } from "./utils/formatPeriod.ts";

const props = defineProps<{
	data: ResumeData;
}>();

const { t, locale } = useI18n();

// Debounce updates to avoid flicker while the form is changing rapidly.
const debouncedData = ref<ResumeData>(props.data);
let debounceTimeout: ReturnType<typeof setTimeout> | null = null;

watch(
	() => props.data,
	(newData) => {
		if (debounceTimeout) {
			clearTimeout(debounceTimeout);
		}
		debounceTimeout = setTimeout(() => {
			debouncedData.value = newData;
		}, 500);
	},
	{ deep: true },
);

onBeforeUnmount(() => {
	if (debounceTimeout) {
		clearTimeout(debounceTimeout);
		debounceTimeout = null;
	}
});

const formatPeriod = (startDate?: string, endDate?: string) => {
	return formatPeriodUtil(
		startDate,
		endDate,
		locale.value,
		t("resume.preview.present"),
	);
};

const hasPersonalInfo = computed(() => {
	const info = debouncedData.value.personalInfo;
	return !!info && !!(info.name || info.email || info.phone || info.location);
});

const hasWorkExperience = computed(() => {
	return (debouncedData.value.workExperience?.length ?? 0) > 0;
});

const hasEducation = computed(() => {
	return (debouncedData.value.education?.length ?? 0) > 0;
});

const hasSkills = computed(() => {
	return (debouncedData.value.skills?.length ?? 0) > 0;
});

const hasLanguages = computed(() => {
	return (debouncedData.value.languages?.length ?? 0) > 0;
});

const hasProjects = computed(() => {
	return (debouncedData.value.projects?.length ?? 0) > 0;
});

const linkedInProfile = computed(() => {
	const profiles = debouncedData.value.personalInfo?.profiles;
	if (!profiles) return null;

	const linkedin = profiles.find(
		(profile) => profile.network?.toLowerCase() === "linkedin",
	);
	return linkedin?.url ?? null;
});
</script>

<template>
	<Card class="resume-preview shadow-lg">
		<CardContent class="p-8 space-y-6">
			<!-- Personal Information Header -->
			<header v-if="hasPersonalInfo" class="text-center border-b pb-4">
				<h1 class="text-3xl font-bold text-foreground">
					{{ debouncedData.personalInfo?.name }}
				</h1>
				<div class="mt-2 text-sm text-muted-foreground space-y-1">
					<p v-if="debouncedData.personalInfo?.email">
						{{ debouncedData.personalInfo.email }}
					</p>
					<p v-if="debouncedData.personalInfo?.phone">
						{{ debouncedData.personalInfo.phone }}
					</p>
														<p v-if="formattedLocation">
															{{ formattedLocation }}
														</p>
					<p v-if="linkedInProfile">
						<a
							:href="linkedInProfile"
							target="_blank"
							rel="noopener noreferrer"
							class="text-primary hover:underline"
						>
							{{ linkedInProfile }}
						</a>
					</p>
				</div>
			</header>

			<!-- Summary -->
			<section v-if="debouncedData.personalInfo?.summary" class="space-y-2">
				<h2 class="text-xl font-semibold border-b pb-1">
					{{ t("resume.preview.summary") }}
				</h2>
				<p class="text-sm leading-relaxed text-foreground">
					{{ debouncedData.personalInfo.summary }}
				</p>
			</section>

			<!-- Work Experience -->
			<section v-if="hasWorkExperience" class="space-y-3">
				<h2 class="text-xl font-semibold border-b pb-1">
					{{ t("resume.preview.workExperience") }}
				</h2>
				<div
					v-for="(job, index) in debouncedData.workExperience"
					:key="index"
					class="space-y-1"
				>
					<div class="flex justify-between items-start">
						<div>
							<h3 class="font-semibold text-foreground">{{ job.position }}</h3>
							<p class="text-sm text-muted-foreground">
								{{ job.company }}
								<span v-if="job.location"> - {{ job.location }}</span>
							</p>
						</div>
						<span class="text-sm text-muted-foreground whitespace-nowrap">
							{{ formatPeriod(job.startDate, job.endDate) }}
						</span>
					</div>
					<p v-if="job.summary" class="text-sm text-foreground">
						{{ job.summary }}
					</p>
					<ul
						v-if="job.highlights && job.highlights.length > 0"
						class="list-disc list-inside text-sm space-y-1"
					>
						<li
							v-for="(highlight, hIndex) in job.highlights"
							:key="hIndex"
							class="text-foreground"
						>
							{{ highlight }}
						</li>
					</ul>
				</div>
			</section>

			<!-- Education -->
			<section v-if="hasEducation" class="space-y-3">
				<h2 class="text-xl font-semibold border-b pb-1">
					{{ t("resume.preview.education") }}
				</h2>
				<div
					v-for="(edu, index) in debouncedData.education"
					:key="index"
					class="space-y-1"
				>
					<div class="flex justify-between items-start">
						<div>
							<h3 class="font-semibold text-foreground">
								{{ edu.studyType }} {{ edu.area }}
							</h3>
							<p class="text-sm text-muted-foreground">
								{{ edu.institution }}
							</p>
						</div>
						<span class="text-sm text-muted-foreground whitespace-nowrap">
							{{ formatPeriod(edu.startDate, edu.endDate) }}
						</span>
					</div>
					<p v-if="edu.score" class="text-sm text-foreground">
						{{ t("resume.preview.gpa") }}: {{ edu.score }}
					</p>
				</div>
			</section>

			<!-- Skills -->
			<section v-if="hasSkills" class="space-y-2">
				<h2 class="text-xl font-semibold border-b pb-1">
					{{ t("resume.preview.skills") }}
				</h2>
				<div class="space-y-2">
					<div
						v-for="(skillCategory, index) in debouncedData.skills"
						:key="index"
						class="text-sm"
					>
						<strong class="text-foreground">{{ skillCategory.name }}:</strong>
						<span class="ml-2 text-muted-foreground">
							{{ skillCategory.keywords?.join(", ") }}
						</span>
					</div>
				</div>
			</section>

			<!-- Languages -->
			<section v-if="hasLanguages" class="space-y-2">
				<h2 class="text-xl font-semibold border-b pb-1">
					{{ t("resume.preview.languages") }}
				</h2>
				<div class="flex flex-wrap gap-4 text-sm">
					<div
						v-for="(lang, index) in debouncedData.languages"
						:key="index"
						class="text-foreground"
					>
						<strong>{{ lang.language }}:</strong>
						<span class="ml-1 text-muted-foreground">{{ lang.fluency }}</span>
					</div>
				</div>
			</section>

			<!-- Projects -->
			<section v-if="hasProjects" class="space-y-3">
				<h2 class="text-xl font-semibold border-b pb-1">
					{{ t("resume.preview.projects") }}
				</h2>
				<div
					v-for="(project, index) in debouncedData.projects"
					:key="index"
					class="space-y-1"
				>
					<div class="flex justify-between items-start">
						<h3 class="font-semibold text-foreground">{{ project.name }}</h3>
						<span v-if="project.startDate" class="text-sm text-muted-foreground whitespace-nowrap">
							{{ formatPeriod(project.startDate, project.endDate) }}
						</span>
					</div>
					<p v-if="project.description" class="text-sm text-foreground">
						{{ project.description }}
					</p>
					<ul
						v-if="project.highlights && project.highlights.length > 0"
						class="list-disc list-inside text-sm space-y-1"
					>
						<li
							v-for="(highlight, hIndex) in project.highlights"
							:key="hIndex"
							class="text-foreground"
						>
							{{ highlight }}
						</li>
					</ul>
					<p v-if="project.url" class="text-sm">
						<a
							:href="project.url"
							target="_blank"
							rel="noopener noreferrer"
							class="text-primary hover:underline"
						>
							{{ project.url }}
						</a>
					</p>
				</div>
			</section>

			<!-- Empty state -->
			<div
				v-if="
					!hasPersonalInfo &&
					!hasWorkExperience &&
					!hasEducation &&
					!hasSkills &&
					!hasLanguages &&
					!hasProjects
				"
				class="text-center py-12 text-muted-foreground"
			>
				<p>{{ t("resume.preview.emptyState") }}</p>
			</div>
		</CardContent>
	</Card>
</template>

<style scoped>
.resume-preview {
	/* PDF-like appearance */
	max-width: 210mm; /* A4 width */
	margin: 0 auto;
	background: var(--card);
	font-family: Georgia, "Times New Roman", serif;
}

.resume-preview h1,
.resume-preview h2,
.resume-preview h3 {
	font-family: inherit;
}
</style>
