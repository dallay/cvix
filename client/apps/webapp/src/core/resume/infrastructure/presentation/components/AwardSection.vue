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
import type { Award } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const awards = defineModel<Award[]>({
	default: () => [],
});

const addAward = () => {
	awards.value.push({
		title: "",
		date: "",
		awarder: "",
		summary: "",
	});
};

const removeAward = (index: number) => {
	awards.value.splice(index, 1);
};

const hasAwards = computed(() => awards.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.awards') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addAward"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addAward') }}
				</Button>
			</div>

			<div
				v-if="hasAwards"
				class="space-y-6"
			>
				<div
					v-for="(award, awardIndex) in awards"
					:key="awardIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.award', { number: awardIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeAward(awardIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`award-title-${awardIndex}`">
									{{ t('resume.fields.awardTitle') }}
								</FieldLabel>
								<Input
									:id="`award-title-${awardIndex}`"
									v-model="award.title"
									type="text"
									:placeholder="t('resume.placeholders.awardTitle')"
									:data-testid="`award-title-${awardIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`award-date-${awardIndex}`">
									{{ t('resume.fields.date') }}
								</FieldLabel>
								<Input
									:id="`award-date-${awardIndex}`"
									v-model="award.date"
									type="date"
									:data-testid="`award-date-${awardIndex}`"
									required
								/>
							</Field>
						</div>

						<Field>
							<FieldLabel :for="`award-awarder-${awardIndex}`">
								{{ t('resume.fields.awarder') }}
							</FieldLabel>
							<Input
								:id="`award-awarder-${awardIndex}`"
								v-model="award.awarder"
								type="text"
								:placeholder="t('resume.placeholders.awarder')"
								:data-testid="`award-awarder-${awardIndex}`"
								required
							/>
						</Field>

						<Field>
							<FieldLabel :for="`award-summary-${awardIndex}`">
								{{ t('resume.fields.summary') }}
							</FieldLabel>
							<Textarea
								:id="`award-summary-${awardIndex}`"
								v-model="award.summary"
								:placeholder="t('resume.placeholders.awardSummary')"
								:rows="3"
								:data-testid="`award-summary-${awardIndex}`"
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
					{{ t('resume.actions.empty.awards') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addAward"
				>
					{{ t('resume.actions.addFirstAward') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>

</style>
