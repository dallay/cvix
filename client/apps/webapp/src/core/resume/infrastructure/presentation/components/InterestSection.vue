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
import type { Interest } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const interests = defineModel<Interest[]>({
	default: () => [],
});

const addInterest = () => {
	interests.value.push({
		name: "",
		keywords: [],
	});
};

const removeInterest = (index: number) => {
	interests.value.splice(index, 1);
};

const addKeyword = (interestIndex: number) => {
	const interest = interests.value[interestIndex];
	if (interest) {
		interest.keywords = [...interest.keywords, ""];
	}
};

const removeKeyword = (interestIndex: number, keywordIndex: number) => {
	const interest = interests.value[interestIndex];
	if (interest) {
		interest.keywords = interest.keywords.filter((_, i) => i !== keywordIndex);
	}
};

const updateKeyword = (
	interestIndex: number,
	keywordIndex: number,
	value: string | number,
) => {
	const interest = interests.value[interestIndex];
	if (interest && typeof value === "string") {
		const newKeywords = [...interest.keywords];
		newKeywords[keywordIndex] = value;
		interest.keywords = newKeywords;
	}
};

const hasInterests = computed(() => interests.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.interests') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addInterest"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addInterest') }}
				</Button>
			</div>

			<div
				v-if="hasInterests"
				class="space-y-6"
			>
				<div
					v-for="(interest, interestIndex) in interests"
					:key="interestIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.interest', { number: interestIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeInterest(interestIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<Field>
							<FieldLabel :for="`interest-name-${interestIndex}`">
								{{ t('resume.fields.interestName') }}
							</FieldLabel>
							<Input
								:id="`interest-name-${interestIndex}`"
								v-model="interest.name"
								type="text"
								:placeholder="t('resume.placeholders.interestName')"
								:data-testid="`interest-name-${interestIndex}`"
								required
							/>
						</Field>

						<div class="space-y-3">
							<div class="flex items-center justify-between">
								<FieldLabel>{{ t('resume.fields.keywords') }}</FieldLabel>
								<Button
									type="button"
									variant="outline"
									size="sm"
									@click="addKeyword(interestIndex)"
								>
									<Plus class="h-4 w-4 mr-2" />
									{{ t('resume.buttons.addKeyword') }}
								</Button>
							</div>

							<div
								v-if="interest.keywords.length > 0"
								class="space-y-2"
							>
								<div
									v-for="(keyword, keywordIndex) in interest.keywords"
									:key="keywordIndex"
									class="flex items-start gap-2"
								>
									<Input
										:id="`interest-keyword-${interestIndex}-${keywordIndex}`"
										:model-value="keyword"
										type="text"
										:placeholder="t('resume.placeholders.keyword')"
										:data-testid="`interest-keyword-${interestIndex}-${keywordIndex}`"
										class="flex-1"
										@update:model-value="updateKeyword(interestIndex, keywordIndex, $event)"
									/>
									<Button
										type="button"
										variant="ghost"
										size="sm"
										@click="removeKeyword(interestIndex, keywordIndex)"
									>
										<Trash2 class="h-4 w-4" />
									</Button>
								</div>
							</div>

							<p
								v-else
								class="text-sm text-muted-foreground italic"
							>
								{{ t('resume.actions.empty.keywords') }}
							</p>
						</div>
					</FieldGroup>
				</div>
			</div>

			<div
				v-else
				class="text-center py-8 border border-dashed border-border rounded-lg bg-muted/50"
			>
				<p class="text-sm text-muted-foreground">
					{{ t('resume.actions.empty.interests') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addInterest"
				>
					{{ t('resume.actions.addFirstInterest') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

