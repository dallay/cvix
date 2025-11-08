<script setup lang="ts">
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldGroup,
	FieldSeparator,
	FieldSet,
} from "@/components/ui/field";
import type {
	Award,
	Certificate,
	Education,
	Interest,
	Language,
	Profile,
	Project,
	Publication,
	Reference,
	Skill,
	Volunteer,
	Work,
} from "@/core/resume/domain/Resume.ts";
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
import ProfilesField from "./ProfilesField.vue";

const profiles = ref<Profile[]>([]);
const workExperiences = ref<Work[]>([]);
const volunteers = ref<Volunteer[]>([]);
const education = ref<Education[]>([]);
const awards = ref<Award[]>([]);
const certificates = ref<Certificate[]>([]);
const publications = ref<Publication[]>([]);
const skills = ref<Skill[]>([]);
const languages = ref<Language[]>([]);
const interests = ref<Interest[]>([]);
const references = ref<Reference[]>([]);
const projects = ref<Project[]>([]);
</script>

<template>
  <div class="w-full">
    <form>
      <FieldGroup>
        <BasicsSection />
        <FieldSeparator />
        <FieldSet>
          <ProfilesField v-model="profiles" />
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
          <Button type="submit">
            Submit
          </Button>
          <Button variant="outline" type="button">
            Cancel
          </Button>
        </Field>
      </FieldGroup>
    </form>
  </div>
</template>
