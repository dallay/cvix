<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useResumeStore } from "@/core/resume-v1/stores/resumeStore.ts";

const { t } = useI18n();
const store = useResumeStore();

const linkedinUrl = computed(
	() =>
		store.resume.basics.profiles?.find(
			(p) => p.network.toLowerCase() === "linkedin",
		)?.url || "",
);

const githubUrl = computed(
	() =>
		store.resume.basics.profiles?.find(
			(p) => p.network.toLowerCase() === "github",
		)?.url || "",
);

function updateField(field: string, value: string | { address: string }) {
	if (field === "location" && typeof value === "object") {
		const currentLocation = store.resume.basics.location ?? {};
		store.updatePersonalInfo({
			...store.resume.basics,
			location: { ...currentLocation, ...value },
		});
		return;
	}
	store.updatePersonalInfo({ ...store.resume.basics, [field]: value });
}

function updateProfile(network: string, url: string) {
	const profiles = store.resume.basics.profiles || [];
	const existing = profiles.findIndex(
		(p) => p.network.toLowerCase() === network.toLowerCase(),
	);

	let updatedProfiles: typeof profiles;
	if (url) {
		const existingProfile = existing >= 0 ? profiles[existing] : undefined;
		const profile = {
			...existingProfile,
			network,
			url,
			username: existingProfile?.username ?? "",
		};
		if (existing >= 0) {
			updatedProfiles = [...profiles];
			updatedProfiles[existing] = profile;
		} else {
			updatedProfiles = [...profiles, profile];
		}
	} else {
		updatedProfiles = profiles.filter(
			(p) => p.network.toLowerCase() !== network.toLowerCase(),
		);
	}

	store.updatePersonalInfo({
		...store.resume.basics,
		profiles: updatedProfiles,
	});
}
</script>

<template>
  <section data-testid="personal-info-section" aria-labelledby="personal-info-heading" class="space-y-4">
    <h2 id="personal-info-heading" class="text-2xl font-semibold">{{ t('resume.sections.personalInfo') }}</h2>

    <div>
      <Label data-testid="fullname-label" for="name">
        {{ t('resume.fields.fullName') }} <span class="text-destructive">*</span>
      </Label>
      <Input
        id="name"
        data-testid="fullname-input"
        type="text"
        :value="store.resume.basics.name"
        :placeholder="t('resume.placeholders.name')"
        maxlength="100"
        @input="updateField('name', ($event.target as HTMLInputElement).value)"
      />
      <p class="text-sm text-muted-foreground mt-1">
        {{ store.resume.basics.name?.length || 0 }}/100
      </p>
    </div>

    <div>
      <Label for="email">{{ t('resume.fields.email') }} <span class="text-destructive">*</span></Label>
      <Input
        id="email"
        data-testid="email-input"
        type="email"
        :value="store.resume.basics.email"
        @input="updateField('email', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div>
      <Label for="phone">{{ t('resume.fields.phone') }}</Label>
      <Input
        id="phone"
        data-testid="phone-input"
        type="tel"
        :value="store.resume.basics.phone"
        @input="updateField('phone', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div>
      <Label for="location">{{ t('resume.fields.location') }}</Label>
      <Input
        id="location"
        data-testid="location-input"
        type="text"
        :value="store.resume.basics.location?.address || ''"
        @input="updateField('location', { address: ($event.target as HTMLInputElement).value })"
      />
    </div>

    <div>
      <Label for="linkedin">{{ t('resume.fields.linkedin') }}</Label>
      <Input
        id="linkedin"
        data-testid="linkedin-input"
        type="url"
        :value="linkedinUrl"
        @input="updateProfile('LinkedIn', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div>
      <Label for="github">{{ t('resume.fields.github') }}</Label>
      <Input
        id="github"
        data-testid="github-input"
        type="url"
        :value="githubUrl"
        @input="updateProfile('GitHub', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div>
      <Label for="website">{{ t('resume.fields.website') }}</Label>
      <Input
        id="website"
        data-testid="website-input"
        type="url"
        :value="store.resume.basics.url"
        @input="updateField('url', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div>
      <Label for="summary">{{ t('resume.fields.summary') }}</Label>
      <Textarea
        id="summary"
        data-testid="summary-textarea"
        :value="store.resume.basics.summary"
        :placeholder="t('resume.placeholders.summary')"
        maxlength="500"
        rows="4"
        @input="updateField('summary', ($event.target as HTMLTextAreaElement).value)"
      />
      <p class="text-sm text-muted-foreground mt-1">
        {{ store.resume.basics.summary?.length || 0 }}/500
      </p>
    </div>
  </section>
</template>
