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
import type { Reference } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const references = defineModel<Reference[]>({
	default: () => [],
});

const addReference = () => {
	references.value.push({
		name: "",
		reference: "",
	});
};

const removeReference = (index: number) => {
	references.value.splice(index, 1);
};

const hasReferences = computed(() => references.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldLegend>{{ t('resume.sections.references') }}</FieldLegend>
		<FieldDescription>
			{{ t('resume.actions.descriptions.references') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addReference"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addReference') }}
				</Button>
			</div>

			<div
				v-if="hasReferences"
				class="space-y-6"
			>
				<div
					v-for="(reference, refIndex) in references"
					:key="refIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.reference', { number: refIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeReference(refIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<Field>
							<FieldLabel :for="`reference-name-${refIndex}`">
								{{ t('resume.fields.referenceName') }}
							</FieldLabel>
							<Input
								:id="`reference-name-${refIndex}`"
								v-model="reference.name"
								type="text"
								:placeholder="t('resume.placeholders.referenceName')"
								:data-testid="`reference-name-${refIndex}`"
								required
							/>
						</Field>

						<Field>
							<FieldLabel :for="`reference-text-${refIndex}`">
								{{ t('resume.fields.referenceText') }}
							</FieldLabel>
							<Textarea
								:id="`reference-text-${refIndex}`"
								v-model="reference.reference"
								:placeholder="t('resume.placeholders.referenceText')"
								:rows="4"
								:data-testid="`reference-text-${refIndex}`"
								required
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
					{{ t('resume.actions.empty.references') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addReference"
				>
					{{ t('resume.actions.addFirstReference') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

