<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import {
	Field,
	FieldDescription,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSeparator,
	FieldSet,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { Profile } from "@/core/resume/domain/Resume.ts";
import ProfilesField from "./ProfilesField.vue";

const { t } = useI18n();

const profiles = ref<Profile[]>([]);
</script>

<template>
  <div class="w-full">
    <form>
      <FieldGroup>
        <FieldSet>
          <FieldLegend>{{ t('resume.sections.personalInfo') }}</FieldLegend>
          <FieldDescription>
            All transactions are secure and encrypted
          </FieldDescription>
          <FieldGroup>
            <Field>
              <FieldLabel for="full-name">
                {{ t('resume.fields.fullName') }}
              </FieldLabel>
              <Input
                  id="full-name"
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
                  data-testid="profile-picture-input"
                  type="file"
                  accept="image/*"
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
                  <Input id="street" type="text" :placeholder="t('resume.placeholders.location.address')" />
                </Field>
                <div class="grid grid-cols-2 gap-4">
                  <Field>
                    <FieldLabel for="city">
                      {{ t('resume.fields.location.city') }}
                    </FieldLabel>
                    <Input id="city" type="text" :placeholder="t('resume.placeholders.location.city')" />
                  </Field>
                  <Field>
                    <FieldLabel for="zip">
                      {{ t('resume.fields.location.postalCode') }}
                    </FieldLabel>
                    <Input id="zip" type="text" :placeholder="t('resume.placeholders.location.postalCode')"  />
                  </Field>
                  <Field>
                    <FieldLabel for="countryCode">
                      {{ t('resume.fields.location.countryCode') }}
                    </FieldLabel>
                    <Input id="countryCode" type="text" :placeholder="t('resume.placeholders.location.countryCode')"  />
                  </Field>
                  <Field>
                    <FieldLabel for="region">
                      {{ t('resume.fields.location.region') }}
                    </FieldLabel>
                    <Input id="region" type="text" :placeholder="t('resume.placeholders.location.region')"  />
                  </Field>
                </div>
              </FieldGroup>
            </FieldSet>
          </FieldGroup>
        </FieldSet>
        <FieldSeparator />
        <FieldSet>
          <ProfilesField v-model="profiles" />
        </FieldSet>
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
