<script setup lang="ts">
import {
	Accordion,
	AccordionContent,
	AccordionItem,
	AccordionTrigger,
} from "@cvix/ui/components/ui/accordion";
import { FieldGroup, FieldSet } from "@cvix/ui/components/ui/field";
import { useI18n } from "vue-i18n";
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
 * Action buttons (Save, Generate PDF, Reset, etc.) have been moved to the
 * parent ResumeEditorPage's sticky header for better UX accessibility.
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
	clearForm,
	loadResume,
} = useResumeForm();

defineExpose({
	loadResume,
	clearForm,
});
</script>

<template>
  <div class="w-full">
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
      </FieldGroup>
  </div>
</template>
