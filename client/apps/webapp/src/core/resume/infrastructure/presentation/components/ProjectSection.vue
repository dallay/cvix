<script setup lang="ts">
import { Button } from "@cvix/ui/components/ui/button";
import { Checkbox } from "@cvix/ui/components/ui/checkbox";
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
import { Textarea } from "@cvix/ui/components/ui/textarea";
import { Plus, Trash2 } from "lucide-vue-next";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { Project } from "@/core/resume/domain/Resume";

const { t, locale } = useI18n();

const projects = defineModel<Project[]>({
	default: () => [],
});

const addProject = () => {
	projects.value.push({
		name: "",
		startDate: "",
		endDate: "",
		description: "",
		highlights: [],
		url: "",
	});
};

const removeProject = (index: number) => {
	projects.value.splice(index, 1);
};

const addHighlight = (projectIndex: number) => {
	const project = projects.value[projectIndex];
	if (project) {
		project.highlights = [...project.highlights, ""];
	}
};

const removeHighlight = (projectIndex: number, highlightIndex: number) => {
	const project = projects.value[projectIndex];
	if (project) {
		project.highlights = project.highlights.filter(
			(_, i) => i !== highlightIndex,
		);
	}
};

const updateHighlight = (
	projectIndex: number,
	highlightIndex: number,
	value: string | number,
) => {
	const project = projects.value[projectIndex];
	if (project && typeof value === "string") {
		const newHighlights = [...project.highlights];
		newHighlights[highlightIndex] = value;
		project.highlights = newHighlights;
	}
};

const toggleCurrent = (index: number) => {
	const project = projects.value[index];
	if (project) {
		if (project.endDate) {
			project.endDate = "";
		} else {
			project.endDate = new Date().toISOString().split("T")[0] || "";
		}
	}
};

const isCurrent = (project: Project) => !project.endDate;

const hasProjects = computed(() => projects.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.projects') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addProject"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addProject') }}
				</Button>
			</div>

			<div
				v-if="hasProjects"
				class="space-y-6"
			>
				<div
					v-for="(project, projectIndex) in projects"
					:key="projectIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.project', { number: projectIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeProject(projectIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`project-name-${projectIndex}`">
									{{ t('resume.fields.projectName') }}
								</FieldLabel>
								<Input
									:id="`project-name-${projectIndex}`"
									v-model="project.name"
									type="text"
									:placeholder="t('resume.placeholders.projectName')"
									:data-testid="`project-name-${projectIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`project-url-${projectIndex}`">
									{{ t('resume.fields.url') }}
								</FieldLabel>
								<Input
									:id="`project-url-${projectIndex}`"
									v-model="project.url"
									type="url"
									:placeholder="t('resume.placeholders.projectUrl')"
									:data-testid="`project-url-${projectIndex}`"
								/>
							</Field>
						</div>

						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`project-start-date-${projectIndex}`">
									{{ t('resume.fields.startDate') }}
								</FieldLabel>
								<DatePicker
									:id="`project-start-date-${projectIndex}`"
									v-model="project.startDate"
									:placeholder="t('resume.placeholders.startDate')"
									:locale="locale"
									:data-testid="`project-start-date-${projectIndex}`"
								/>
							</Field>

							<Field>
								<FieldLabel :for="`project-end-date-${projectIndex}`">
									{{ t('resume.fields.endDate') }}
								</FieldLabel>
								<DatePicker
									:id="`project-end-date-${projectIndex}`"
									v-model="project.endDate"
									:placeholder="t('resume.placeholders.endDate')"
									:locale="locale"
									:disabled="isCurrent(project)"
									:data-testid="`project-end-date-${projectIndex}`"
								/>
							</Field>
						</div>

						<div class="flex items-center gap-2">
							<Checkbox
								:id="`project-current-${projectIndex}`"
								:model-value="isCurrent(project)"
								@update:model-value="toggleCurrent(projectIndex)"
							/>
							<FieldLabel
								:for="`project-current-${projectIndex}`"
								class="mb-0! cursor-pointer font-normal"
							>
								{{ t('resume.fields.currentProject') }}
							</FieldLabel>
						</div>

						<Field>
							<FieldLabel :for="`project-description-${projectIndex}`">
								{{ t('resume.fields.description') }}
							</FieldLabel>
							<Textarea
								:id="`project-description-${projectIndex}`"
								v-model="project.description"
								:placeholder="t('resume.placeholders.projectDescription')"
								:rows="4"
								:data-testid="`project-description-${projectIndex}`"
								required
							/>
						</Field>

						<div class="space-y-3">
							<div class="flex items-center justify-between">
								<FieldLabel>{{ t('resume.fields.highlights') }}</FieldLabel>
								<Button
									type="button"
									variant="outline"
									size="sm"
									@click="addHighlight(projectIndex)"
								>
									<Plus class="h-4 w-4 mr-2" />
									{{ t('resume.buttons.addHighlight') }}
								</Button>
							</div>

							<div
								v-if="project.highlights.length > 0"
								class="space-y-2"
							>
								<div
									v-for="(highlight, highlightIndex) in project.highlights"
									:key="highlightIndex"
									class="flex items-start gap-2"
								>
									<Input
										:id="`project-highlight-${projectIndex}-${highlightIndex}`"
										:model-value="highlight"
										type="text"
										:placeholder="t('resume.placeholders.projectHighlight')"
										:data-testid="`project-highlight-${projectIndex}-${highlightIndex}`"
										class="flex-1"
										@update:model-value="updateHighlight(projectIndex, highlightIndex, $event)"
									/>
									<Button
										type="button"
										variant="ghost"
										size="sm"
										@click="removeHighlight(projectIndex, highlightIndex)"
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
					{{ t('resume.actions.empty.projects') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addProject"
				>
					{{ t('resume.actions.addFirstProject') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

