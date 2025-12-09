<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Resume } from "@/core/resume/domain/Resume";
import ResumePreviewEducation from "./resume-preview/ResumePreviewEducation.vue";
import ResumePreviewHeader from "./resume-preview/ResumePreviewHeader.vue";
import ResumePreviewLanguages from "./resume-preview/ResumePreviewLanguages.vue";
import ResumePreviewProjects from "./resume-preview/ResumePreviewProjects.vue";
import ResumePreviewSection from "./resume-preview/ResumePreviewSection.vue";
import ResumePreviewSkills from "./resume-preview/ResumePreviewSkills.vue";
import ResumePreviewWork from "./resume-preview/ResumePreviewWork.vue";

interface Props {
	data: Resume;
}

const props = defineProps<Props>();
const { t } = useI18n();

const emit =
	defineEmits<
		(e: "navigate-section", section: string, entryIndex?: number) => void
	>();

const hasWork = computed(() => props.data.work?.length > 0);
const hasEducation = computed(() => props.data.education?.length > 0);
const hasSkills = computed(() => props.data.skills?.length > 0);
const hasLanguages = computed(() => props.data.languages?.length > 0);
const hasProjects = computed(() => props.data.projects?.length > 0);
const hasVolunteer = computed(() => props.data.volunteer?.length > 0);
const hasCertificates = computed(() => props.data.certificates?.length > 0);
const hasAwards = computed(() => props.data.awards?.length > 0);
</script>

<template>
  <div class="resume-preview bg-background">
    <div class="mx-auto max-w-[210mm] bg-card shadow-sm">
      <ResumePreviewHeader :basics="props.data.basics" />
      <div class="space-y-6 p-8">
        <ResumePreviewSection
          v-if="props.data.basics.summary"
          :title="t('resume.preview.sections.summary')"
          @click="emit('navigate-section', 'basics')"
        >
          <p class="text-sm text-foreground leading-relaxed whitespace-pre-line">
            {{ props.data.basics.summary }}
          </p>
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasWork"
          :title="t('resume.preview.sections.experience')"
          @click="emit('navigate-section', 'work')"
          data-section="work"
        >
          <div class="space-y-4">
            <ResumePreviewWork
              v-for="(work, index) in props.data.work"
              :key="`work-${index}`"
              :work="work"
              @click.stop="emit('navigate-section', 'work', index)"
              :data-section="'work'"
              :data-entry-id="index"
            />
          </div>
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasEducation"
          :title="t('resume.preview.sections.education')"
          @click="emit('navigate-section', 'education')"
          data-section="education"
        >
          <div class="space-y-4">
            <ResumePreviewEducation
              v-for="(edu, index) in props.data.education"
              :key="`edu-${index}`"
              :education="edu"
              @click.stop="emit('navigate-section', 'education', index)"
              :data-section="'education'"
              :data-entry-id="index"
            />
          </div>
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasSkills"
          :title="t('resume.preview.sections.skills')"
          @click="emit('navigate-section', 'skills')"
        >
          <ResumePreviewSkills :skills="props.data.skills" />
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasProjects"
          :title="t('resume.preview.sections.projects')"
          @click="emit('navigate-section', 'projects')"
          data-section="projects"
        >
          <div class="space-y-4">
            <ResumePreviewProjects
              v-for="(project, index) in props.data.projects"
              :key="`project-${index}`"
              :project="project"
              @click.stop="emit('navigate-section', 'projects', index)"
              :data-section="'projects'"
              :data-entry-id="index"
            />
          </div>
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasLanguages"
          :title="t('resume.preview.sections.languages')"
          @click="emit('navigate-section', 'languages')"
        >
          <ResumePreviewLanguages :languages="props.data.languages" />
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasVolunteer"
          :title="t('resume.preview.sections.volunteer')"
          @click="emit('navigate-section', 'volunteer')"
          data-section="volunteer"
        >
          <div class="space-y-4">
            <div
              v-for="(vol, index) in props.data.volunteer"
              :key="`volunteer-${index}`"
              class="space-y-1"
              @click.stop="emit('navigate-section', 'volunteer', index)"
              :data-section="'volunteer'"
              :data-entry-id="index"
            >
              <div class="flex justify-between items-baseline">
                <h4 class="font-medium text-foreground">{{ vol.position }}</h4>
                <span class="text-xs text-muted-foreground">
                  {{ vol.startDate }} - {{ vol.endDate || t('resume.preview.present') }}
                </span>
              </div>
              <p class="text-sm text-muted-foreground">{{ vol.organization }}</p>
              <p v-if="vol.summary" class="text-sm text-foreground mt-1">{{ vol.summary }}</p>
              <ul v-if="vol.highlights?.length" class="list-disc list-inside text-sm text-foreground mt-2 space-y-1">
                <li v-for="(highlight, hIndex) in vol.highlights" :key="hIndex">{{ highlight }}</li>
              </ul>
            </div>
          </div>
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasCertificates"
          :title="t('resume.preview.sections.certificates')"
          @click="emit('navigate-section', 'certificates')"
          data-section="certificates"
        >
          <div class="space-y-2">
            <div
              v-for="(cert, index) in props.data.certificates"
              :key="`cert-${index}`"
              class="flex justify-between items-baseline"
              @click.stop="emit('navigate-section', 'certificates', index)"
              :data-section="'certificates'"
              :data-entry-id="index"
            >
              <div>
                <h4 class="font-medium text-foreground">{{ cert.name }}</h4>
                <p class="text-sm text-muted-foreground">{{ cert.issuer }}</p>
              </div>
              <span class="text-xs text-muted-foreground">{{ cert.date }}</span>
            </div>
          </div>
        </ResumePreviewSection>
        <ResumePreviewSection
          v-if="hasAwards"
          :title="t('resume.preview.sections.awards')"
          @click="emit('navigate-section', 'awards')"
          data-section="awards"
        >
          <div class="space-y-3">
            <div
              v-for="(award, index) in props.data.awards"
              :key="`award-${index}`"
              class="space-y-1"
              @click.stop="emit('navigate-section', 'awards', index)"
              :data-section="'awards'"
              :data-entry-id="index"
            >
              <div class="flex justify-between items-baseline">
                <h4 class="font-medium text-foreground">{{ award.title }}</h4>
                <span class="text-xs text-muted-foreground">{{ award.date }}</span>
              </div>
              <p class="text-sm text-muted-foreground">{{ award.awarder }}</p>
              <p v-if="award.summary" class="text-sm text-foreground">{{ award.summary }}</p>
            </div>
          </div>
        </ResumePreviewSection>
      </div>
    </div>
  </div>
</template>

<style scoped>
.resume-preview {
  font-size: 14px;
  line-height: 1.5;
}
.resume-preview .max-w-\[210mm\] {
  min-height: 297mm;
}
@media print {
  .resume-preview {
    background: white;
  }
  .resume-preview .max-w-\[210mm\] {
    box-shadow: none;
    max-width: 100%;
  }
}
</style>
