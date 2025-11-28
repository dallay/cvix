<script lang="ts" setup>
import { isEqual } from "@loomify/utilities";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import type {
	ParamValue,
	TemplateMetadata,
} from "@/core/resume/domain/TemplateMetadata";

interface Props {
	templates: TemplateMetadata[];
	modelValue: {
		templateId: string;
		params: Record<string, ParamValue>;
	};
}

const props = defineProps<Props>();
const emit = defineEmits<{
	"update:modelValue": [
		value: { templateId: string; params: Record<string, ParamValue> },
	];
}>();

const { t } = useI18n();

const selectedTemplateId = ref(props.modelValue.templateId);
const params = ref<Record<string, ParamValue>>({ ...props.modelValue.params });

const templateSelectId = `template-select-${Math.random().toString(36).slice(2, 9)}`;

// Hardcoded options since backend doesn't provide them yet
const SUPPORTED_FONTS = [
	"Roboto",
	"Open Sans",
	"Lato",
	"Montserrat",
	"Merriweather",
];
const SUPPORTED_COLORS = ["blue", "green", "red", "purple", "orange", "gray"];

// Sync external templateId changes
watch(
	() => props.modelValue.templateId,
	(newId) => {
		if (newId !== selectedTemplateId.value) {
			selectedTemplateId.value = newId;
		}
	},
);

// Sync external params changes
watch(
	() => props.modelValue.params,
	(newParams) => {
		if (!isEqual(newParams, params.value)) {
			params.value = { ...newParams };
		}
	},
);

const selectedTemplate = computed(() =>
	props.templates.find((t) => t.id === selectedTemplateId.value),
);

// Reset params when template changes
watch(selectedTemplateId, (newId) => {
	if (newId) {
		const newParams: Record<string, ParamValue> = {};
		// Set defaults if available in template params
		if (selectedTemplate.value?.params) {
			const defaults = selectedTemplate.value.params;
			if (defaults.colorPalette) newParams.colorPalette = defaults.colorPalette;
			if (defaults.fontFamily) newParams.fontFamily = defaults.fontFamily;
			if (defaults.spacing) newParams.spacing = defaults.spacing;
			if (defaults.density) newParams.density = defaults.density;

			// Copy custom params defaults
			if (defaults.customParams) {
				Object.entries(defaults.customParams).forEach(([key, val]) => {
					newParams[key] = val as ParamValue;
				});
			}
		}

		// Ensure locale is set if not already
		if (!newParams.locale && selectedTemplate.value?.supportedLocales?.length) {
			// Default to first supported locale (usually EN)
			newParams.locale = selectedTemplate.value.supportedLocales[0];
		}

		params.value = newParams;
	}
});

// Emit params changes
watch(params, (newParams) => {
	if (!isEqual(newParams, props.modelValue.params)) {
		emit("update:modelValue", {
			templateId: selectedTemplateId.value,
			params: { ...newParams },
		});
	}
});

// Helper to safely cast param to string for Select
const getParamString = (key: string): string => {
	const val = params.value[key];
	return val === undefined || val === null ? "" : String(val);
};

// Helper to update param
const updateParam = (key: string, value: unknown) => {
	params.value[key] = value as ParamValue;
};
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-2">
      <Label :for="templateSelectId">{{ t('resume.pdfSelector.templateLabel', 'Template') }}</Label>
      <Select v-model="selectedTemplateId">
        <SelectTrigger :id="templateSelectId">
          <SelectValue :placeholder="t('resume.pdfSelector.selectTemplate', 'Select a template')"/>
        </SelectTrigger>
        <SelectContent>
          <SelectItem
              v-for="template in templates"
              :key="template.id"
              :value="template.id"
          >
            {{ template.name }}
          </SelectItem>
        </SelectContent>
      </Select>
      <p v-if="selectedTemplate?.description" class="text-xs text-muted-foreground">
        {{ selectedTemplate.description }}
      </p>
    </div>

    <div v-if="selectedTemplate" class="space-y-4 border-t pt-4">
      <h3 class="text-sm font-semibold">
        {{ t('resume.pdfSelector.coreOptions', 'Appearance & Language') }}</h3>

      <!-- Locale -->
      <div v-if="selectedTemplate.supportedLocales?.length" class="space-y-2">
        <Label for="locale">{{ t('resume.pdfSelector.param.locale', 'Language') }}</Label>
        <Select
            :model-value="getParamString('locale')"
            @update:model-value="(val) => updateParam('locale', val)"
        >
          <SelectTrigger id="locale">
            <SelectValue :placeholder="t('resume.pdfSelector.selectLocale', 'Select language')"/>
          </SelectTrigger>
          <SelectContent>
            <SelectItem
                v-for="opt in selectedTemplate.supportedLocales"
                :key="opt"
                :value="opt"
            >
              {{ opt }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <!-- Font Family -->
      <div class="space-y-2">
        <Label for="fontFamily">{{
            t('resume.pdfSelector.param.fontFamily', 'Font Family')
          }}</Label>
        <Select
            :model-value="getParamString('fontFamily')"
            @update:model-value="(val) => updateParam('fontFamily', val)"
        >
          <SelectTrigger id="fontFamily">
            <SelectValue :placeholder="t('resume.pdfSelector.selectFont', 'Select font')"/>
          </SelectTrigger>
          <SelectContent>
            <SelectItem
                v-for="opt in SUPPORTED_FONTS"
                :key="opt"
                :value="opt"
            >
              {{ opt }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <!-- Color Palette -->
      <div class="space-y-2">
        <Label for="colorPalette">{{
            t('resume.pdfSelector.param.colorPalette', 'Color Scheme')
          }}</Label>
        <Select
            :model-value="getParamString('colorPalette')"
            @update:model-value="(val) => updateParam('colorPalette', val)"
        >
          <SelectTrigger id="colorPalette">
            <SelectValue :placeholder="t('resume.pdfSelector.selectColor', 'Select color')"/>
          </SelectTrigger>
          <SelectContent>
            <SelectItem
                v-for="opt in SUPPORTED_COLORS"
                :key="opt"
                :value="opt"
            >
              {{ opt }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <!-- Custom Params (if any in defaults) -->
      <div v-if="selectedTemplate.params?.customParams" class="space-y-4 border-t pt-4">
        <h3 class="text-sm font-semibold">
          {{ t('resume.pdfSelector.optionsTitle', 'Template Options') }}</h3>
        <div v-for="(val, key) in selectedTemplate.params.customParams" :key="key"
             class="flex items-center space-x-2 py-2">
          <!-- Simple boolean toggle for now as no schema types -->
          <Checkbox
              :id="String(key)"
              :checked="Boolean(params[key])"
              @update:checked="(checked: boolean) => updateParam(String(key), checked)"
          />
          <Label :for="String(key)" class="cursor-pointer capitalize">
            {{ key.replace(/([A-Z])/g, ' $1').trim() }}
          </Label>
        </div>
      </div>

    </div>
  </div>
</template>
