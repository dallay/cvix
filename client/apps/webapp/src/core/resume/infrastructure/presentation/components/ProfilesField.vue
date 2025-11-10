<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldDescription,
	FieldGroup,
	FieldLabel,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import type { Profile } from "@/core/resume/domain/Resume.ts";

const { t } = useI18n();

const profiles = defineModel<Profile[]>({
	default: () => [],
});

const addProfile = () => {
	profiles.value.push({
		network: "",
		username: "",
		url: "",
	});
};

const removeProfile = (index: number) => {
	profiles.value.splice(index, 1);
};

const hasProfiles = computed(() => profiles.value.length > 0);
</script>

<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <FieldLabel>{{ t('resume.fields.profiles') }}</FieldLabel>
        <FieldDescription>
          {{ t('resume.actions.descriptions.profiles') }}
        </FieldDescription>
      </div>
      <Button
        type="button"
        variant="outline"
        size="sm"
        @click="addProfile"
      >
        <Plus class="h-4 w-4 mr-2" />
        {{ t('resume.actions.addProfile') }}
      </Button>
    </div>

    <div
      v-if="hasProfiles"
      class="space-y-4"
    >
      <div
        v-for="(profile, index) in profiles"
        :key="index"
        class="border border-border rounded-lg p-4 space-y-4 bg-card"
      >
        <div class="flex items-center justify-between">
          <h4 class="text-sm font-medium">
            {{ t('resume.actions.labels.profile', { number: index + 1 }) }}
          </h4>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            @click="removeProfile(index)"
          >
            <Trash2 class="h-4 w-4" />
          </Button>
        </div>

        <FieldGroup>
          <Field>
            <FieldLabel :for="`profile-network-${index}`">
              {{ t('resume.fields.network') }}
            </FieldLabel>
            <Input
              :id="`profile-network-${index}`"
              v-model="profile.network"
              type="text"
              :placeholder="t('resume.placeholders.network')"
              :data-testid="`profile-network-${index}`"
            />
          </Field>

          <Field>
            <FieldLabel :for="`profile-username-${index}`">
              {{ t('resume.fields.username') }}
            </FieldLabel>
            <Input
              :id="`profile-username-${index}`"
              v-model="profile.username"
              type="text"
              :placeholder="t('resume.placeholders.username')"
              :data-testid="`profile-username-${index}`"
            />
          </Field>

          <Field>
            <FieldLabel :for="`profile-url-${index}`">
              {{ t('resume.fields.profileUrl') }}
            </FieldLabel>
            <Input
              :id="`profile-url-${index}`"
              v-model="profile.url"
              type="url"
              :placeholder="t('resume.placeholders.profileUrl')"
              :data-testid="`profile-url-${index}`"
            />
          </Field>
        </FieldGroup>
      </div>
    </div>

    <div
      v-else
      class="text-center py-8 border border-dashed border-border rounded-lg bg-muted/50"
    >
      <p class="text-sm text-muted-foreground">
        {{ t('resume.actions.empty.profiles') }}
      </p>
      <Button
        type="button"
        variant="link"
        size="sm"
        class="mt-2"
        @click="addProfile"
      >
        {{ t('resume.actions.addFirstProfile') }}
      </Button>
    </div>
  </div>
</template>
