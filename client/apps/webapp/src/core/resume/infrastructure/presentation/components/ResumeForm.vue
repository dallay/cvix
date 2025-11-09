<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { toast } from "vue-sonner";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldGroup,
	FieldSeparator,
	FieldSet,
} from "@/components/ui/field";
import AwardSection from "@/core/resume/infrastructure/presentation/components/AwardSection.vue";
import BasicsSection from "@/core/resume/infrastructure/presentation/components/BasicsSection.vue";
import CertificateSection from "@/core/resume/infrastructure/presentation/components/CertificateSection.vue";
import EducationSection from "@/core/resume/infrastructure/presentation/components/EducationSection.vue";
import InterestSection from "@/core/resume/infrastructure/presentation/components/InterestSection.vue";
import LanguageSection from "@/core/resume/infrastructure/presentation/components/LanguageSection.vue";
import ProjectSection from "@/core/resume/infrastructure/presentation/components/ProjectSection.vue";
import PublicationSection from "@/core/resume/infrastructure/presentation/components/PublicationSection.vue";
import ReferenceSection from "@/core/resume/infrastructure/presentation/components/ReferenceSection.vue";
import SkillSection from "@/core/resume/infrastructure/presentation/components/SkillSection.vue";
import VolunteerSection from "@/core/resume/infrastructure/presentation/components/VolunteerSection.vue";
import WorkExperienceSection from "@/core/resume/infrastructure/presentation/components/WorkExperienceSection.vue";
import { useResumeForm } from "@/core/resume/infrastructure/presentation/composables/useResumeForm";
import ProfilesField from "./ProfilesField.vue";

const { t } = useI18n();

const {
	basics,
	workExperiences,
	volunteers,
	education,
	awards,
	certificates,
	publications,
	skills,
	languages,
	interests,
	references,
	projects,
	isValid,
	isGenerating,
	generationError,
	submitResume,
	generatePdf,
	clearForm,
	saveToStorage,
} = useResumeForm();

const isSubmitting = ref(false);

/**
 * Handles form submission
 */
async function handleSubmit(event: Event) {
	event.preventDefault();
	isSubmitting.value = true;
	try {
		const valid = submitResume();
		if (!valid) {
			toast.error(t("resume.toast.validationError.title"), {
				description: t("resume.toast.validationError.description"),
			});
			isSubmitting.value = false;
			return;
		}

		// Save to storage after validation
		await saveToStorage();

		toast.success(t("resume.toast.saveSuccess.title"), {
			description: t("resume.toast.saveSuccess.description"),
		});
		isSubmitting.value = false;
	} catch (error) {
		toast.error(t("resume.toast.saveError.title"), {
			description: t("resume.toast.saveError.description"),
		});
		isSubmitting.value = false;
	}
}

/**
 * Handles PDF generation
 */
async function handleGeneratePdf() {
	try {
		const pdfBlob = await generatePdf("en");
		const url = URL.createObjectURL(pdfBlob);
		const link = document.createElement("a");
		link.href = url;
		link.download = "resume.pdf";
		link.click();
		URL.revokeObjectURL(url);
		toast.success(t("resume.toast.pdfSuccess.title"), {
			description: t("resume.toast.pdfSuccess.description"),
		});
	} catch (error) {
		toast.error(t("resume.toast.pdfError.title"), {
			description:
				generationError.value?.detail || t("resume.toast.pdfError.description"),
		});
	}
}

/**
 * Handles form cancellation/reset
 */
function handleCancel() {
	if (confirm(t("resume.form.confirmClear"))) {
		clearForm();
		toast.success(t("resume.toast.formCleared.title"), {
			description: t("resume.toast.formCleared.description"),
		});
	}
}
</script>

<template>
  <div class="w-full">
    <form @submit="handleSubmit">
      <FieldGroup>
        <BasicsSection />
        <FieldSeparator />
        <FieldSet>
          <ProfilesField v-model="basics.profiles" />
        </FieldSet>
        <FieldSeparator />
        <WorkExperienceSection v-model="workExperiences" />
        <FieldSeparator />
        <VolunteerSection v-model="volunteers" />
        <FieldSeparator />
        <EducationSection v-model="education" />
        <FieldSeparator />
        <AwardSection v-model="awards" />
        <FieldSeparator />
        <CertificateSection v-model="certificates" />
        <FieldSeparator />
        <PublicationSection v-model="publications" />
        <FieldSeparator />
        <SkillSection v-model="skills" />
        <FieldSeparator />
        <LanguageSection v-model="languages" />
        <FieldSeparator />
        <InterestSection v-model="interests" />
        <FieldSeparator />
        <ReferenceSection v-model="references" />
        <FieldSeparator />
        <ProjectSection v-model="projects" />
        <FieldSeparator />
        <Field orientation="horizontal">
          <Button type="submit" :disabled="isSubmitting || isGenerating">
            {{ isSubmitting ? t("resume.form.saving") : t("resume.form.submit") }}
          </Button>
          <Button
            variant="outline"
            type="button"
            :disabled="!isValid || isGenerating"
            @click="handleGeneratePdf"
          >
            {{ isGenerating ? t("resume.form.generating") : t("resume.form.generatePdf") }}
          </Button>
          <Button
            variant="outline"
            type="button"
            @click="handleCancel"
          >
            {{ t("resume.form.cancel") }}
          </Button>
        </Field>
      </FieldGroup>
    </form>
  </div>
</template>
