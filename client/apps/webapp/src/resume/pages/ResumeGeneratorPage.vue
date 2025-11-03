<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import ResumeForm from "../components/ResumeForm.vue";
import ResumePreview from "../components/ResumePreview.vue";
import { useResumeStore } from "../stores/resumeStore";

const { t } = useI18n();
const resumeStore = useResumeStore();
const isGenerating = ref(false);
const showPreview = ref(true);

// Get resume data from store (already transformed to ResumeData format)
const resumeData = computed(() => resumeStore.resumeData);

const handleGenerate = async () => {
	isGenerating.value = true;
	try {
		// The form component handles the actual generation
	} finally {
		isGenerating.value = false;
	}
};

const togglePreview = () => {
	showPreview.value = !showPreview.value;
};
</script>

<template>
	<div class="container mx-auto py-8 px-4">
		<div class="mb-4 flex justify-between items-center">
			<div>
				<h1 class="text-3xl font-bold text-foreground">
					{{ t("resume.title") }}
				</h1>
				<p class="text-muted-foreground">
					{{ t("resume.subtitle") }}
				</p>
			</div>
			<Button
				variant="outline"
				@click="togglePreview"
				class="hidden lg:flex"
			>
				<svg
					class="h-4 w-4 mr-2"
					fill="none"
					stroke="currentColor"
					viewBox="0 0 24 24"
				>
					<path
						v-if="showPreview"
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"
					/>
					<g v-else>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
						/>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
						/>
					</g>
				</svg>
				{{ showPreview ? t('resume.preview.hide') : t('resume.previewButton') }}
			</Button>
		</div>

		<div class="grid gap-6" :class="showPreview ? 'lg:grid-cols-2' : 'lg:grid-cols-1'">
			<!-- Form Section -->
			<Card class="h-fit">
				<CardHeader>
					<CardTitle>
						{{ t("resume.sections.personalInfo") }}
					</CardTitle>
					<CardDescription>
						Fill out the form to create your resume
					</CardDescription>
				</CardHeader>
				<CardContent>
					<ResumeForm @generate="handleGenerate" />
				</CardContent>
			</Card>

			<!-- Preview Section -->
			<Card v-if="showPreview" class="hidden lg:block sticky top-8 h-fit max-h-[calc(100vh-6rem)] overflow-hidden">
				<CardHeader>
					<CardTitle>
						{{ t('resume.previewButton') }}
					</CardTitle>
					<CardDescription>
						Live preview of your resume
					</CardDescription>
				</CardHeader>
				<CardContent class="p-0">
					<div class="max-h-[calc(100vh-16rem)] overflow-y-auto">
						<ResumePreview :data="resumeData" />
					</div>
				</CardContent>
			</Card>
		</div>
	</div>
</template>
