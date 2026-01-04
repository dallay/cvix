<script setup lang="ts">
import {
	Field,
	FieldDescription,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSet,
} from "@cvix/ui/components/ui/field";
import { Input } from "@cvix/ui/components/ui/input";
import { Textarea } from "@cvix/ui/components/ui/textarea";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Basics, Location } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const basics = defineModel<Basics>({ required: true });

// Ensure location is never null for v-model binding
const location = computed<Location>({
	get() {
		// If location is null, initialize it
		if (!basics.value.location) {
			basics.value.location = {
				address: "",
				postalCode: "",
				city: "",
				countryCode: "",
				region: "",
			};
		}
		return basics.value.location;
	},
	set(newValue: Location) {
		basics.value.location = newValue;
	},
});
</script>

<template>
  <FieldSet>
    <FieldDescription>
      {{ t('resume.actions.descriptions.personalInfo') }}
    </FieldDescription>
    <FieldGroup>
      <Field>
        <FieldLabel for="full-name">
          {{ t('resume.fields.fullName') }}
        </FieldLabel>
        <Input
            id="full-name"
            v-model="basics.name"
            data-testid="fullname-input"
            type="text"
            :placeholder="t('resume.placeholders.name')"
            maxlength="100"
            required
        />
      </Field>
      <Field>
        <FieldLabel for="label-short-description">
          {{ t('resume.fields.label') }}
        </FieldLabel>
        <Input
            id="label-short-description"
            v-model="basics.label"
            data-testid="label-short-description-input"
            type="text"
            :placeholder="t('resume.placeholders.label')"
            maxlength="100"
            required
        />
      </Field>
      <Field>
        <FieldLabel for="profile-picture">
          {{ t('resume.fields.image') }}
        </FieldLabel>
        <Input
            id="profile-picture"
            v-model="basics.image"
            data-testid="profile-picture-input"
            type="text"
            :placeholder="t('resume.placeholders.image')"
            required
        />
        <FieldDescription>
          {{ t('resume.placeholders.imageHint') }}
        </FieldDescription>
      </Field>
      <Field>
        <FieldLabel for="email">
          {{ t('resume.fields.email') }}
        </FieldLabel>
        <Input
            id="email"
            v-model="basics.email"
            data-testid="email-input"
            type="email"
            :placeholder="t('resume.placeholders.email')"
            required
        />
      </Field>
      <Field>
        <FieldLabel for="phone">
          {{ t('resume.fields.phone') }}
        </FieldLabel>
        <Input
            id="phone"
            v-model="basics.phone"
            data-testid="phone-input"
            type="tel"
            :placeholder="t('resume.placeholders.phone')"
        />
      </Field>
      <Field>
        <FieldLabel for="url">
          {{ t('resume.fields.url') }}
        </FieldLabel>
        <Input
            id="url"
            v-model="basics.url"
            data-testid="url-input"
            type="url"
            :placeholder="t('resume.placeholders.url')"
            required
        />
      </Field>
      <Field>
        <FieldLabel for="summary">
          {{ t('resume.fields.summary') }}
        </FieldLabel>
        <Textarea
            id="summary"
            v-model="basics.summary"
            :placeholder="t('resume.placeholders.summary')"
            :rows="4"
        />
      </Field>
      <FieldSet>
        <FieldGroup>
          <Field>
            <FieldLabel for="street">
              {{ t('resume.fields.location.address') }}
            </FieldLabel>
            <Input
              id="street"
              v-model="location.address"
              type="text"
              :placeholder="t('resume.placeholders.location.address')"
            />
          </Field>
          <div class="grid grid-cols-2 gap-4">
            <Field>
              <FieldLabel for="city">
                {{ t('resume.fields.location.city') }}
              </FieldLabel>
              <Input
                id="city"
                v-model="location.city"
                type="text"
                :placeholder="t('resume.placeholders.location.city')"
              />
            </Field>
            <Field>
              <FieldLabel for="zip">
                {{ t('resume.fields.location.postalCode') }}
              </FieldLabel>
              <Input
                id="zip"
                v-model="location.postalCode"
                type="text"
                :placeholder="t('resume.placeholders.location.postalCode')"
              />
            </Field>
            <Field>
              <FieldLabel for="countryCode">
                {{ t('resume.fields.location.countryCode') }}
              </FieldLabel>
              <Input
                id="countryCode"
                v-model="location.countryCode"
                type="text"
                :placeholder="t('resume.placeholders.location.countryCode')"
              />
            </Field>
            <Field>
              <FieldLabel for="region">
                {{ t('resume.fields.location.region') }}
              </FieldLabel>
              <Input
                id="region"
                v-model="location.region"
                type="text"
                :placeholder="t('resume.placeholders.location.region')"
              />
            </Field>
          </div>
        </FieldGroup>
      </FieldSet>
    </FieldGroup>
  </FieldSet>
</template>

<style scoped>

</style>
