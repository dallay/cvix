<script setup lang="ts">
import { Button } from "@cvix/ui/components/ui/button";
import { DatePicker } from "@cvix/ui/components/ui/date-picker";
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
import type { Education } from "@/core/resume/domain/Resume";

const { t, locale } = useI18n();

const educationEntries = defineModel<Education[]>({
	default: () => [],
});

const addEducation = () => {
	educationEntries.value.push({
		institution: "",
		url: "",
		area: "",
		studyType: "",
		startDate: "",
		endDate: "",
		score: "",
		courses: [],
	});
};

const removeEducation = (index: number) => {
	educationEntries.value.splice(index, 1);
};

const addCourse = (educationIndex: number) => {
	const education = educationEntries.value[educationIndex];
	if (education) {
		education.courses = [...education.courses, ""];
	}
};

const removeCourse = (educationIndex: number, courseIndex: number) => {
	const education = educationEntries.value[educationIndex];
	if (education) {
		education.courses = education.courses.filter((_, i) => i !== courseIndex);
	}
};

const updateCourse = (
	educationIndex: number,
	courseIndex: number,
	value: string | number,
) => {
	const education = educationEntries.value[educationIndex];
	if (education && typeof value === "string") {
		const newCourses = [...education.courses];
		newCourses[courseIndex] = value;
		education.courses = newCourses;
	}
};

const hasEducation = computed(() => educationEntries.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.education') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addEducation"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addEducation') }}
				</Button>
			</div>

			<div
				v-if="hasEducation"
				class="space-y-6"
			>
				<div
					v-for="(education, educationIndex) in educationEntries"
					:key="educationIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.education', { number: educationIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeEducation(educationIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`education-institution-${educationIndex}`">
									{{ t('resume.fields.institution') }}
								</FieldLabel>
								<Input
									:id="`education-institution-${educationIndex}`"
									v-model="education.institution"
									type="text"
									:placeholder="t('resume.placeholders.institution')"
									:data-testid="`education-institution-${educationIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`education-url-${educationIndex}`">
									{{ t('resume.fields.url') }}
								</FieldLabel>
								<Input
									:id="`education-url-${educationIndex}`"
									v-model="education.url"
									type="url"
									:placeholder="t('resume.placeholders.url')"
									:data-testid="`education-url-${educationIndex}`"
								/>
							</Field>
						</div>

						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`education-area-${educationIndex}`">
									{{ t('resume.fields.area') }}
								</FieldLabel>
								<Input
									:id="`education-area-${educationIndex}`"
									v-model="education.area"
									type="text"
									:placeholder="t('resume.placeholders.area')"
									:data-testid="`education-area-${educationIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`education-studyType-${educationIndex}`">
									{{ t('resume.fields.studyType') }}
								</FieldLabel>
								<Input
									:id="`education-studyType-${educationIndex}`"
									v-model="education.studyType"
									type="text"
									:placeholder="t('resume.placeholders.studyType')"
									:data-testid="`education-studyType-${educationIndex}`"
									required
								/>
							</Field>
						</div>

						<div class="grid grid-cols-1 md:grid-cols-3 gap-4">
							<Field>
								<FieldLabel :for="`education-start-date-${educationIndex}`">
									{{ t('resume.fields.startDate') }}
								</FieldLabel>
								<DatePicker
									:id="`education-start-date-${educationIndex}`"
									v-model="education.startDate"
									:placeholder="t('resume.placeholders.startDate')"
									:locale="locale"
									:data-testid="`education-start-date-${educationIndex}`"
								/>
							</Field>

							<Field>
								<FieldLabel :for="`education-end-date-${educationIndex}`">
									{{ t('resume.fields.endDate') }}
								</FieldLabel>
								<DatePicker
									:id="`education-end-date-${educationIndex}`"
									v-model="education.endDate"
									:placeholder="t('resume.placeholders.endDate')"
									:locale="locale"
									:data-testid="`education-end-date-${educationIndex}`"
								/>
							</Field>

							<Field>
								<FieldLabel :for="`education-score-${educationIndex}`">
									{{ t('resume.fields.score') }}
								</FieldLabel>
								<Input
									:id="`education-score-${educationIndex}`"
									v-model="education.score"
									type="text"
									:placeholder="t('resume.placeholders.score')"
									:data-testid="`education-score-${educationIndex}`"
								/>
							</Field>
						</div>

						<div class="space-y-3">
							<div class="flex items-center justify-between">
								<FieldLabel>{{ t('resume.fields.courses') }}</FieldLabel>
								<Button
									type="button"
									variant="outline"
									size="sm"
									@click="addCourse(educationIndex)"
								>
									<Plus class="h-4 w-4 mr-2" />
									{{ t('resume.buttons.addCourse') }}
								</Button>
							</div>

							<div
								v-if="education.courses.length > 0"
								class="space-y-2"
							>
								<div
									v-for="(course, courseIndex) in education.courses"
									:key="courseIndex"
									class="flex items-start gap-2"
								>
									<Input
										:id="`education-course-${educationIndex}-${courseIndex}`"
										:model-value="course"
										type="text"
										:placeholder="t('resume.placeholders.course')"
										:data-testid="`education-course-${educationIndex}-${courseIndex}`"
										class="flex-1"
										@update:model-value="updateCourse(educationIndex, courseIndex, $event)"
									/>
									<Button
										type="button"
										variant="ghost"
										size="sm"
										@click="removeCourse(educationIndex, courseIndex)"
									>
										<Trash2 class="h-4 w-4" />
									</Button>
								</div>
							</div>

							<p
								v-else
								class="text-sm text-muted-foreground italic"
							>
								{{ t('resume.actions.empty.courses') }}
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
					{{ t('resume.actions.empty.education') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addEducation"
				>
					{{ t('resume.actions.addFirstEducation') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>

</style>
