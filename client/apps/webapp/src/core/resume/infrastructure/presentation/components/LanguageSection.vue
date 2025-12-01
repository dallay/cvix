<script setup lang="ts">
import { Button } from "@cvix/ui/components/ui/button";
import {
	Field,
	FieldDescription,
	FieldGroup,
	FieldLabel,
	FieldLegend,
	FieldSet,
} from "@cvix/ui/components/ui/field";
import { Input } from "@cvix/ui/components/ui/input";
import { Plus, Trash2 } from "lucide-vue-next";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Language } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const languages = defineModel<Language[]>({
	default: () => [],
});

const addLanguage = () => {
	languages.value.push({
		language: "",
		fluency: "",
	});
};

const removeLanguage = (index: number) => {
	languages.value.splice(index, 1);
};

const hasLanguages = computed(() => languages.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.languages') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addLanguage"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addLanguage') }}
				</Button>
			</div>

			<div
				v-if="hasLanguages"
				class="space-y-6"
			>
				<div
					v-for="(language, langIndex) in languages"
					:key="langIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.language', { number: langIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeLanguage(langIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`language-name-${langIndex}`">
									{{ t('resume.fields.language') }}
								</FieldLabel>
								<Input
									:id="`language-name-${langIndex}`"
									v-model="language.language"
									type="text"
									:placeholder="t('resume.placeholders.language')"
									:data-testid="`language-name-${langIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`language-fluency-${langIndex}`">
									{{ t('resume.fields.fluency') }}
								</FieldLabel>
								<Input
									:id="`language-fluency-${langIndex}`"
									v-model="language.fluency"
									type="text"
									:placeholder="t('resume.placeholders.fluency')"
									:data-testid="`language-fluency-${langIndex}`"
									required
								/>
							</Field>
						</div>
					</FieldGroup>
				</div>
			</div>

			<div
				v-else
				class="text-center py-8 border border-dashed border-border rounded-lg bg-muted/50"
			>
				<p class="text-sm text-muted-foreground">
					{{ t('resume.actions.empty.languages') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addLanguage"
				>
					{{ t('resume.actions.addFirstLanguage') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

