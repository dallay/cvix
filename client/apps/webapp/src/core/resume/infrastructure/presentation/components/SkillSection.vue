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
import type { Skill } from "@/core/resume/domain/Resume";

const { t } = useI18n();

const skills = defineModel<Skill[]>({
	default: () => [],
});

const addSkill = () => {
	skills.value.push({
		name: "",
		level: "",
		keywords: [],
	});
};

const removeSkill = (index: number) => {
	skills.value.splice(index, 1);
};

const addKeyword = (skillIndex: number) => {
	const skill = skills.value[skillIndex];
	if (skill) {
		skill.keywords = [...skill.keywords, ""];
	}
};

const removeKeyword = (skillIndex: number, keywordIndex: number) => {
	const skill = skills.value[skillIndex];
	if (skill) {
		skill.keywords = skill.keywords.filter((_, i) => i !== keywordIndex);
	}
};

const updateKeyword = (
	skillIndex: number,
	keywordIndex: number,
	value: string | number,
) => {
	const skill = skills.value[skillIndex];
	if (skill && typeof value === "string") {
		const newKeywords = [...skill.keywords];
		newKeywords[keywordIndex] = value;
		skill.keywords = newKeywords;
	}
};

const hasSkills = computed(() => skills.value.length > 0);
</script>

<template>
	<FieldSet>
		<FieldDescription>
			{{ t('resume.actions.descriptions.skills') }}
		</FieldDescription>

		<div class="space-y-4 mt-4">
			<div class="flex items-center justify-end">
				<Button
					type="button"
					variant="outline"
					size="sm"
					@click="addSkill"
				>
					<Plus class="h-4 w-4 mr-2" />
					{{ t('resume.buttons.addSkill') }}
				</Button>
			</div>

			<div
				v-if="hasSkills"
				class="space-y-6"
			>
				<div
					v-for="(skill, skillIndex) in skills"
					:key="skillIndex"
					class="border border-border rounded-lg p-6 space-y-4 bg-card"
				>
					<div class="flex items-center justify-between">
						<h4 class="text-sm font-medium">
							{{ t('resume.actions.labels.skill', { number: skillIndex + 1 }) }}
						</h4>
						<Button
							type="button"
							variant="ghost"
							size="sm"
							@click="removeSkill(skillIndex)"
						>
							<Trash2 class="h-4 w-4" />
						</Button>
					</div>

					<FieldGroup>
						<div class="grid grid-cols-1 md:grid-cols-2 gap-4">
							<Field>
								<FieldLabel :for="`skill-name-${skillIndex}`">
									{{ t('resume.fields.skillName') }}
								</FieldLabel>
								<Input
									:id="`skill-name-${skillIndex}`"
									v-model="skill.name"
									type="text"
									:placeholder="t('resume.placeholders.skillName')"
									:data-testid="`skill-name-${skillIndex}`"
									required
								/>
							</Field>

							<Field>
								<FieldLabel :for="`skill-level-${skillIndex}`">
									{{ t('resume.fields.skillLevel') }}
								</FieldLabel>
								<Input
									:id="`skill-level-${skillIndex}`"
									v-model="skill.level"
									type="text"
									:placeholder="t('resume.placeholders.skillLevel')"
									:data-testid="`skill-level-${skillIndex}`"
								/>
							</Field>
						</div>

						<div class="space-y-3">
							<div class="flex items-center justify-between">
								<FieldLabel>{{ t('resume.fields.keywords') }}</FieldLabel>
								<Button
									type="button"
									variant="outline"
									size="sm"
									@click="addKeyword(skillIndex)"
								>
									<Plus class="h-4 w-4 mr-2" />
									{{ t('resume.buttons.addKeyword') }}
								</Button>
							</div>

							<div
								v-if="skill.keywords.length > 0"
								class="space-y-2"
							>
								<div
									v-for="(keyword, keywordIndex) in skill.keywords"
									:key="keywordIndex"
									class="flex items-start gap-2"
								>
									<Input
										:id="`skill-keyword-${skillIndex}-${keywordIndex}`"
										:model-value="keyword"
										type="text"
										:placeholder="t('resume.placeholders.keyword')"
										:data-testid="`skill-keyword-${skillIndex}-${keywordIndex}`"
										class="flex-1"
										@update:model-value="updateKeyword(skillIndex, keywordIndex, $event)"
									/>
									<Button
										type="button"
										variant="ghost"
										size="sm"
										@click="removeKeyword(skillIndex, keywordIndex)"
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
					{{ t('resume.actions.empty.skills') }}
				</p>
				<Button
					type="button"
					variant="link"
					size="sm"
					class="mt-2"
					@click="addSkill"
				>
					{{ t('resume.actions.addFirstSkill') }}
				</Button>
			</div>
		</div>
	</FieldSet>
</template>

<style scoped>
</style>

