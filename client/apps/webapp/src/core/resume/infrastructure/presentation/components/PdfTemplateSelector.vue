<script lang="ts" setup>
import { isEqual } from "@loomify/utilities";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { Label } from "@/components/ui/label";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import {
	SUPPORTED_COLORS,
	SUPPORTED_FONTS,
} from "@/core/resume/domain/TemplateConstants.ts";
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
	isLoading?: boolean;
	error?: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
	"update:modelValue": [
		value: { templateId: string; params: Record<string, ParamValue> },
	];
	"params:changed": [params: Record<string, ParamValue>];
}>();

const { t } = useI18n();

const selectedTemplateId = ref(props.modelValue.templateId);
const params = ref<Record<string, ParamValue>>({ ...props.modelValue.params });

const templateSelectId = `template-select-${crypto.randomUUID()}`;

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

// Helper: Validate if the input should be processed
function isValidTemplateId(value: ParamValue): boolean {
	if (value === null || value === "" || props.templates.length === 0) {
		return false;
	}
	// Ignore object values, only process primitives
	return typeof value !== "object";
}

// Helper: Convert input to string ID
function convertToStringId(value: ParamValue): string {
	if (typeof value === "bigint" || typeof value === "number") {
		return value.toString();
	}
	if (typeof value === "string") {
		return value;
	}
	return "";
}

// Helper: Build params from template defaults
function buildTemplateParams(
	template: TemplateMetadata | undefined,
): Record<string, ParamValue> {
	const newParams: Record<string, ParamValue> = {};

	if (!template?.params) {
		return newParams;
	}

	const defaults = template.params;

	// Copy known params
	if (defaults.colorPalette) newParams.colorPalette = defaults.colorPalette;
	if (defaults.fontFamily) newParams.fontFamily = defaults.fontFamily;
	if (defaults.spacing) newParams.spacing = defaults.spacing;
	if (defaults.density) newParams.density = defaults.density;

	// Copy custom params
	if (defaults.customParams) {
		Object.entries(defaults.customParams).forEach(([key, val]) => {
			newParams[key] = val as ParamValue;
		});
	}

	// Set default locale if not present
	if (!newParams.locale && template.supportedLocales?.length) {
		newParams.locale = template.supportedLocales[0] || "en";
	}

	return newParams;
}

// Accept all AcceptableValue types (string | number | bigint | null), ignore objects
function onUserTemplateChange(newId: ParamValue) {
	if (!isValidTemplateId(newId)) {
		return;
	}

	const id = convertToStringId(newId);
	if (!id) {
		return;
	}

	selectedTemplateId.value = id;
	const template = props.templates.find((t) => t.id === id);
	params.value = buildTemplateParams(template);
}

// Emit params changes
watch(params, (newParams) => {
	if (!isEqual(newParams, props.modelValue.params)) {
		const snapshot = { ...newParams };
		emit("update:modelValue", {
			templateId: selectedTemplateId.value,
			params: snapshot,
		});
		emit("params:changed", snapshot);
	}
});

// Helper to safely cast param to string for Select
const getParamString = (key: string): string => {
	const val = params.value[key];
	return val === undefined || val === null ? "" : String(val);
};

const updateParam = (key: string, value: unknown) => {
	let safeValue: ParamValue;
	if (value === null) {
		safeValue = "";
	} else if (typeof value === "bigint") {
		safeValue = value.toString();
	} else if (
		typeof value === "string" ||
		typeof value === "number" ||
		typeof value === "boolean" ||
		value === undefined
	) {
		safeValue = value as ParamValue;
	} else {
		// fallback for unsupported types
		safeValue = "";
	}
	params.value[key] = safeValue;
};
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-2">
      <Label :for="templateSelectId">{{ t('resume.pdfSelector.templateLabel', 'Template') }}</Label>
      <Select
          v-model="selectedTemplateId"
          :disabled="props.isLoading || templates.length === 0"
          @update:model-value="onUserTemplateChange"
          aria-label="Resume template selector"
      >
        <SelectTrigger :id="templateSelectId">
          <SelectValue :placeholder="t('resume.pdfSelector.selectTemplate', 'Select a template')"/>
        </SelectTrigger>
        <SelectContent>
          <template v-if="props.isLoading">
            <SelectItem disabled value="">
              {{ t('resume.pdfSelector.loading', 'Loading templates…') }}
            </SelectItem>
          </template>
          <template v-else-if="props.error">
            <SelectItem disabled value="">
              {{ props.error }}
            </SelectItem>
          </template>
          <template v-else-if="templates.length === 0">
            <SelectItem disabled value="">
              {{ t('resume.pdfSelector.noTemplates', 'No templates available') }}
            </SelectItem>
          </template>
          <template v-else>
            <SelectItem
                v-for="template in templates"
                :key="template.id"
                :value="template.id"
            >
              {{ template.name }}
            </SelectItem>
          </template>
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
            aria-label="Resume language selector"
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
            aria-label="Resume font selector"
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
            aria-label="Resume color selector"
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

    </div>
  </div>
</template>
