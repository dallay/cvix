<script setup lang="ts">
import { Plus, Trash2 } from "lucide-vue-next";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
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
import type { Volunteer } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const volunteers = defineModel<Volunteer[]>({
	default: () => [],
});

const addVolunteer = () => {
	volunteers.value.push({
		organization: "",
		position: "",
		url: "",
		startDate: "",
		endDate: "",
		summary: "",
		highlights: [],
	});
};

const removeVolunteer = (index: number) => {
	volunteers.value.splice(index, 1);
};

const addHighlight = (volunteerIndex: number) => {
	const volunteer = volunteers.value[volunteerIndex];
	if (volunteer) {
		volunteer.highlights = [...volunteer.highlights, ""];
	}
};

const removeHighlight = (volunteerIndex: number, highlightIndex: number) => {
	const volunteer = volunteers.value[volunteerIndex];
	if (volunteer) {
		volunteer.highlights = volunteer.highlights.filter(
			(_, i) => i !== highlightIndex,
		);
	}
};

const updateHighlight = (
	volunteerIndex: number,
	highlightIndex: number,
	value: string | number,
) => {
	const volunteer = volunteers.value[volunteerIndex];
	if (volunteer && typeof value === "string") {
		const newHighlights = [...volunteer.highlights];
		newHighlights[highlightIndex] = value;
		volunteer.highlights = newHighlights;
	}
};

const toggleCurrent = (index: number) => {
	const volunteer = volunteers.value[index];
	if (volunteer) {
		if (volunteer.endDate) {
			volunteer.endDate = "";
		} else {
			// Set to current date when unchecking
			volunteer.endDate = new Date().toISOString().split("T")[0] || "";
		}
	}
};

const isCurrent = (volunteer: Volunteer) => !volunteer.endDate;

const hasVolunteers = computed(() => volunteers.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldLegend>{{ t('resume.sections.volunteer') }}</FieldLegend>
		<FieldDescription>
			{{ t('resume.actions.descriptions.volunteer') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addVolunteer"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addVolunteer') }}
				</Button>
			</div>

			<div
				v-if="hasVolunteers"
				class="space-y-6"
			>
				<div
					v-for="(volunteer, volunteerIndex) in volunteers"
					:key="volunteerIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.volunteer', { number: volunteerIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeVolunteer(volunteerIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`volunteer-organization-${volunteerIndex}`">
									{{ t('resume.fields.organization') }}
								</FieldLabel>
								<Input
									:id="`volunteer-organization-${volunteerIndex}`"
									v-model="volunteer.organization"
									type="text"
									:placeholder="t('resume.placeholders.organization')"
									:data-testid="`volunteer-organization-${volunteerIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`volunteer-position-${volunteerIndex}`">
									{{ t('resume.fields.position') }}
								</FieldLabel>
								<Input
									:id="`volunteer-position-${volunteerIndex}`"
									v-model="volunteer.position"
									type="text"
									:placeholder="t('resume.placeholders.volunteerPosition')"
									:data-testid="`volunteer-position-${volunteerIndex}`"
									required
								/>
							</Field>
						</div>

						<Field>
							<FieldLabel :for="`volunteer-url-${volunteerIndex}`">
								{{ t('resume.fields.organizationUrl') }}
							</FieldLabel>
							<Input
								:id="`volunteer-url-${volunteerIndex}`"
								v-model="volunteer.url"
								type="url"
								:placeholder="t('resume.placeholders.organizationUrl')"
								:data-testid="`volunteer-url-${volunteerIndex}`"
							/>
						</Field>

						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`volunteer-start-date-${volunteerIndex}`">
									{{ t('resume.fields.startDate') }}
								</FieldLabel>
								<Input
									:id="`volunteer-start-date-${volunteerIndex}`"
									v-model="volunteer.startDate"
									type="date"
									:data-testid="`volunteer-start-date-${volunteerIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`volunteer-end-date-${volunteerIndex}`">
									{{ t('resume.fields.endDate') }}
								</FieldLabel>
								<Input
									:id="`volunteer-end-date-${volunteerIndex}`"
									v-model="volunteer.endDate"
									type="date"
									:data-testid="`volunteer-end-date-${volunteerIndex}`"
									:disabled="isCurrent(volunteer)"
								/>
							</Field>
						</div>

						<div class="flex items-center gap-2">
							<Checkbox
								:id="`volunteer-current-${volunteerIndex}`"
								:checked="isCurrent(volunteer)"
								@update:checked="toggleCurrent(volunteerIndex)"
							/>
							<FieldLabel
								:for="`volunteer-current-${volunteerIndex}`"
								class="mb-0! cursor-pointer font-normal"
							>
								{{ t('resume.fields.currentVolunteer') }}
							</FieldLabel>
						</div>

						<Field>
							<FieldLabel :for="`volunteer-summary-${volunteerIndex}`">
								{{ t('resume.fields.summary') }}
							</FieldLabel>
							<Textarea
								:id="`volunteer-summary-${volunteerIndex}`"
								v-model="volunteer.summary"
								:placeholder="t('resume.placeholders.volunteerSummary')"
								:rows="4"
								:data-testid="`volunteer-summary-${volunteerIndex}`"
							/>
						</Field>

						<div class="space-y-3">
							<div class="flex items-center justify-between">
								<FieldLabel>{{ t('resume.fields.highlights') }}</FieldLabel>
								<Button
									type="button"
									variant="outline"
									size="sm"
									@click="addHighlight(volunteerIndex)"
								>
									<Plus class="h-4 w-4 mr-2" />
									{{ t('resume.buttons.addHighlight') }}
								</Button>
							</div>

							<div
								v-if="volunteer.highlights.length > 0"
								class="space-y-2"
							>
								<div
									v-for="(highlight, highlightIndex) in volunteer.highlights"
									:key="highlightIndex"
									class="flex items-start gap-2"
								>
									<Input
										:id="`volunteer-highlight-${volunteerIndex}-${highlightIndex}`"
										:model-value="highlight"
										type="text"
										:placeholder="t('resume.placeholders.volunteerHighlight')"
										:data-testid="`volunteer-highlight-${volunteerIndex}-${highlightIndex}`"
										class="flex-1"
										@update:model-value="updateHighlight(volunteerIndex, highlightIndex, $event)"
									/>
									<Button
										type="button"
										variant="ghost"
										size="sm"
										@click="removeHighlight(volunteerIndex, highlightIndex)"
									>
										<Trash2 class="h-4 w-4" />
									</Button>
								</div>
							</div>

							<p
								v-else
								class="text-sm text-muted-foreground italic"
							>
								{{ t('resume.actions.empty.highlights') }}
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
					{{ t('resume.actions.empty.volunteer') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addVolunteer"
				>
					{{ t('resume.actions.addFirstVolunteer') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>
