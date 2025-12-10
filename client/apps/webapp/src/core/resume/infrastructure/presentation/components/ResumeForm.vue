<script setup lang="ts">
import {
	Accordion,
	AccordionContent,
	AccordionItem,
	AccordionTrigger,
} from "@cvix/ui/components/ui/accordion";
import { Button } from "@cvix/ui/components/ui/button";
import { Field, FieldGroup, FieldSet } from "@cvix/ui/components/ui/field";
import { Loader2 } from "lucide-vue-next";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { toast } from "vue-sonner";
import type { Resume } from "@/core/resume/domain/Resume";
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

/**
 * Component relies entirely on the shared reactive state from useResumeForm().
 * The composable is the source of truth; all form fields are bound via v-model
 * to refs exported by the composable. Parent pages/components update the
 * composable state directly, and this component automatically reflects changes.
 *
 * No defineExpose needed: all state updates flow through the composable's
 * reactive refs, eliminating the need for imperative parent-to-child method calls.
 */
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
	loadResume,
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
			return;
		}

		// Save to storage after validation
		await saveToStorage();

		toast.success(t("resume.toast.saveSuccess.title"), {
			description: t("resume.toast.saveSuccess.description"),
		});
	} catch (error) {
		console.error("Error submitting resume:", error);
		toast.error(t("resume.toast.saveError.title"), {
			description: t("resume.toast.saveError.description"),
		});
	} finally {
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
		console.error("Error generating PDF:", error);
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
        <Accordion type="multiple" class="w-full" :default-value="['basics', 'work', 'education']">
          <AccordionItem value="basics">
              <AccordionTrigger>{{ t("resume.sections.personalDetails") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-basics" class="space-y-6">
                  <BasicsSection v-model="basics" />
                  <FieldSet>
                    <ProfilesField v-model="basics.profiles" />
                  </FieldSet>
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="work">
              <AccordionTrigger>{{ t("resume.sections.work") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-work">
                  <WorkExperienceSection v-model="workExperiences" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="education">
              <AccordionTrigger>{{ t("resume.sections.education") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-education">
                  <EducationSection v-model="education" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="skills">
              <AccordionTrigger>{{ t("resume.sections.skills") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-skills">
                  <SkillSection v-model="skills" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="projects">
              <AccordionTrigger>{{ t("resume.sections.projects") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-projects">
                  <ProjectSection v-model="projects" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="languages">
              <AccordionTrigger>{{ t("resume.sections.languages") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-languages">
                  <LanguageSection v-model="languages" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="volunteer">
              <AccordionTrigger>{{ t("resume.sections.volunteer") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-volunteer">
                  <VolunteerSection v-model="volunteers" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="certificates">
              <AccordionTrigger>{{ t("resume.sections.certificates") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-certificates">
                  <CertificateSection v-model="certificates" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="awards">
              <AccordionTrigger>{{ t("resume.sections.awards") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-awards">
                  <AwardSection v-model="awards" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="publications">
              <AccordionTrigger>{{ t("resume.sections.publications") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-publications">
                  <PublicationSection v-model="publications" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="interests">
              <AccordionTrigger>{{ t("resume.sections.interests") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-interests">
                  <InterestSection v-model="interests" />
                </div>
              </AccordionContent>
          </AccordionItem>

          <AccordionItem value="references">
              <AccordionTrigger>{{ t("resume.sections.references") }}</AccordionTrigger>
              <AccordionContent>
                <div ref="section-references">
                  <ReferenceSection v-model="references" />
                </div>
              </AccordionContent>
          </AccordionItem>
        </Accordion>

        <div class="mt-6">
          <Field orientation="horizontal">
            <Button type="submit" :disabled="isSubmitting || isGenerating">
              <Loader2 v-if="isSubmitting" class="mr-2 h-4 w-4 animate-spin" />
              {{ isSubmitting ? t("resume.form.saving") : t("resume.form.submit") }}
            </Button>
            <Button
              variant="outline"
              type="button"
              :disabled="!isValid || isGenerating"
              @click="handleGeneratePdf"
            >
              <Loader2 v-if="isGenerating" class="mr-2 h-4 w-4 animate-spin" />
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
        </div>
      </FieldGroup>
    </form>
  </div>
</template>
