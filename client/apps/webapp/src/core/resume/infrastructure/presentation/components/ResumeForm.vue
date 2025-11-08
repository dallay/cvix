<script setup lang="ts">
import { ref } from "vue";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldGroup,
	FieldSeparator,
	FieldSet,
} from "@/components/ui/field";
import type { Profile, Volunteer, Work } from "@/core/resume/domain/Resume.ts";
import BasicsSection from "@/core/resume/infrastructure/presentation/components/BasicsSection.vue";
import VolunteerSection from "@/core/resume/infrastructure/presentation/components/VolunteerSection.vue";
import WorkExperienceSection from "@/core/resume/infrastructure/presentation/components/WorkExperienceSection.vue";
import ProfilesField from "./ProfilesField.vue";

const profiles = ref<Profile[]>([]);
const workExperiences = ref<Work[]>([]);
const volunteers = ref<Volunteer[]>([]);
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
