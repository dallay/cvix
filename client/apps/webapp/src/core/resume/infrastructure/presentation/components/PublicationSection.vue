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
	FieldLegend,
	FieldSet,
} from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import type { Publication } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const publications = defineModel<Publication[]>({
	default: () => [],
});

const addPublication = () => {
	publications.value.push({
		name: "",
		publisher: "",
		releaseDate: "",
		url: "",
		summary: "",
	});
};

const removePublication = (index: number) => {
	publications.value.splice(index, 1);
};

const hasPublications = computed(() => publications.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.publications') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addPublication"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addPublication') }}
				</Button>
			</div>

			<div
				v-if="hasPublications"
				class="space-y-6"
			>
				<div
					v-for="(publication, pubIndex) in publications"
					:key="pubIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.publication', { number: pubIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removePublication(pubIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`publication-name-${pubIndex}`">
									{{ t('resume.fields.publicationName') }}
								</FieldLabel>
								<Input
									:id="`publication-name-${pubIndex}`"
									v-model="publication.name"
									type="text"
									:placeholder="t('resume.placeholders.publicationName')"
									:data-testid="`publication-name-${pubIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`publication-publisher-${pubIndex}`">
									{{ t('resume.fields.publisher') }}
								</FieldLabel>
								<Input
									:id="`publication-publisher-${pubIndex}`"
									v-model="publication.publisher"
									type="text"
									:placeholder="t('resume.placeholders.publisher')"
									:data-testid="`publication-publisher-${pubIndex}`"
									required
								/>
							</Field>
						</div>

						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`publication-release-date-${pubIndex}`">
									{{ t('resume.fields.releaseDate') }}
								</FieldLabel>
								<Input
									:id="`publication-release-date-${pubIndex}`"
									v-model="publication.releaseDate"
									type="date"
									:data-testid="`publication-release-date-${pubIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`publication-url-${pubIndex}`">
									{{ t('resume.fields.url') }}
								</FieldLabel>
								<Input
									:id="`publication-url-${pubIndex}`"
									v-model="publication.url"
									type="url"
									:placeholder="t('resume.placeholders.publicationUrl')"
									:data-testid="`publication-url-${pubIndex}`"
								/>
							</Field>
						</div>

						<Field>
							<FieldLabel :for="`publication-summary-${pubIndex}`">
								{{ t('resume.fields.summary') }}
							</FieldLabel>
							<Textarea
								:id="`publication-summary-${pubIndex}`"
								v-model="publication.summary"
								:placeholder="t('resume.placeholders.publicationSummary')"
								:rows="3"
								:data-testid="`publication-summary-${pubIndex}`"
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
					{{ t('resume.actions.empty.publications') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addPublication"
				>
					{{ t('resume.actions.addFirstPublication') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

