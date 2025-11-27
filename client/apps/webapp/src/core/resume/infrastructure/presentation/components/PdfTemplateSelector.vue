<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";

interface Props {
	templates: TemplateMetadata[];
	modelValue: {
		templateId: string;
		params: Record<string, any>;
	};
}

const props = defineProps<Props>();
const emit = defineEmits<{
	"update:modelValue": [
		value: { templateId: string; params: Record<string, any> },
	];
}>();

const { t } = useI18n();

const selectedTemplateId = ref(props.modelValue.templateId);
const params = ref<Record<string, any>>({ ...props.modelValue.params });

// Generate a stable-ish id for the main select per component instance for accessibility
const templateSelectId = `template-select-${Math.random().toString(36).slice(2, 9)}`;

// Helper: convert internal param value to string for form controls
function toModelValue(val: unknown, _prop: any) {
	if (val === undefined || val === null) return "";
	// numbers and booleans are converted to string for the UI
	return String(val);
}

// Helper: coerce string value from UI back to typed param according to schema prop
// biome-ignore lint/suspicious/noExplicitAny: AcceptableValue from reka-ui can be any type
function fromModelValue(raw: any, prop: any) {
	const strValue = raw == null ? "" : String(raw);
	if (strValue === "") return undefined;
	if (!prop) return strValue;
	if (prop.type === "number") {
		const n = Number(strValue);
		return Number.isNaN(n) ? undefined : n;
	}
	// For enums, detect underlying type from first enum item
	if (prop.enum && Array.isArray(prop.enum) && prop.enum.length > 0) {
		const first = prop.enum[0];
		if (typeof first === "number") {
			const n = Number(strValue);
			return Number.isNaN(n) ? undefined : n;
		}
	}
	return strValue;
}

// Watch for external changes to modelValue.templateId (e.g. initial selection from parent)
watch(
	() => props.modelValue.templateId,
	(newId) => {
		if (newId !== selectedTemplateId.value) {
			selectedTemplateId.value = newId;
		}
	},
);

// Watch for external changes to modelValue.params (sync props -> local params)
watch(
	() => props.modelValue.params,
	(newParams) => {
		params.value = { ...newParams };
	},
	{ deep: true },
);

const selectedTemplate = computed(() =>
	props.templates.find((t) => t.id === selectedTemplateId.value),
);

const schema = computed(() => {
	if (!selectedTemplate.value?.paramsSchema) return null;
	try {
		return JSON.parse(selectedTemplate.value.paramsSchema);
	} catch (e) {
		console.error("Failed to parse template schema", e);
		return null;
	}
});

// Reset params when template changes
watch(selectedTemplateId, (newId) => {
	if (newId) {
		// Initialize default values from schema
		const newParams: Record<string, any> = {};
		if (schema.value?.properties) {
			Object.entries(schema.value.properties).forEach(
				([key, prop]: [string, any]) => {
					if (prop.default !== undefined) {
						newParams[key] = prop.default;
					}
				},
			);
		}
		params.value = newParams;
		emit("update:modelValue", { templateId: newId, params: newParams });
	}
});

// Emit changes when params change
watch(
	params,
	(newParams) => {
		emit("update:modelValue", {
			templateId: selectedTemplateId.value,
			params: { ...newParams },
		});
	},
	{ deep: true },
);
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
      <p v-if="selectedTemplate" class="text-xs text-muted-foreground">
        {{ selectedTemplate.description }}
      </p>
    </div>

    <div v-if="schema && schema.properties" class="space-y-4 border-t pt-4">
      <h3 class="text-sm font-semibold">{{
          t('resume.pdfSelector.optionsTitle', 'Template Options')
        }}</h3>
      <div v-for="(prop, key) in schema.properties" :key="key" class="space-y-2">
        <!-- Enum Select -->
        <div v-if="prop.enum" class="space-y-2">
          <Label :for="String(key)">{{
              t(`resume.pdfSelector.param.${key}`, prop.description || key)
            }}</Label>
          <Select
              :model-value="toModelValue(params[key], prop)"
              @update:model-value="(val: any) => (params[key] = fromModelValue(val, prop))"
          >
            <SelectTrigger :id="String(key)">
              <SelectValue
                  :placeholder="t(`resume.pdfSelector.param.${key}`, prop.description || key)"/>
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                  v-for="opt in prop.enum"
                  :key="opt"
                  :value="String(opt)"
              >
                {{ opt }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
        <!-- Boolean Checkbox -->
        <div v-else-if="prop.type === 'boolean'" class="flex items-center space-x-2 py-2">
          <Checkbox
              :id="String(key)"
              :checked="params[key]"
              @update:checked="(checked: boolean) => params[key] = checked"
          />
          <Label :for="String(key)" class="cursor-pointer">{{
              t(`resume.pdfSelector.param.${key}`, prop.description || key)
            }}</Label>
        </div>
        <!-- String/Number Input -->
        <div v-else class="space-y-2">
          <Label :for="String(key)">{{
              t(`resume.pdfSelector.param.${key}`, prop.description || key)
            }}</Label>
          <Input
              :id="String(key)"
              :model-value="toModelValue(params[key], prop)"
              :type="prop.type === 'number' ? 'number' : 'text'"
              @update:model-value="(val: any) => (params[key] = fromModelValue(val, prop))"
          />
        </div>
      </div>
    </div>
  </div>
</template>
