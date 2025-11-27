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

// JSON Schema Types
interface SchemaPropertyBase {
	description?: string;
	default?: unknown;
}

interface StringProperty extends SchemaPropertyBase {
	type: "string";
	enum?: string[];
}

interface NumberProperty extends SchemaPropertyBase {
	type: "number";
	enum?: number[];
}

interface BooleanProperty extends SchemaPropertyBase {
	type: "boolean";
}

interface EnumProperty extends SchemaPropertyBase {
	enum: (string | number)[];
	type?: "string" | "number";
}

type SchemaProperty =
	| StringProperty
	| NumberProperty
	| BooleanProperty
	| EnumProperty;

interface JSONSchema {
	properties?: Record<string, SchemaProperty>;
}

type ParamValue = string | number | boolean | undefined;

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

// Type guards
type PropertyWithEnum = SchemaProperty & { enum: (string | number)[] };

function hasEnum(prop: SchemaProperty): prop is PropertyWithEnum {
	return "enum" in prop && Array.isArray(prop.enum);
}

function isNumberProperty(prop: SchemaProperty): prop is NumberProperty {
	return prop.type === "number";
}

function isBooleanProperty(prop: SchemaProperty): prop is BooleanProperty {
	return prop.type === "boolean";
}

// Convert internal value to string for form controls
function toModelValue(val: ParamValue): string {
	if (val === undefined || val === null) return "";
	return String(val);
}

// Coerce string from UI back to typed param
function fromModelValue(raw: unknown, prop: SchemaProperty): ParamValue {
	const strValue = raw == null ? "" : String(raw);
	if (strValue === "") return undefined;

	if (isNumberProperty(prop)) {
		const n = Number(strValue);
		return Number.isNaN(n) ? undefined : n;
	}

	if (hasEnum(prop) && prop.enum.length > 0) {
		const first = prop.enum[0];
		if (typeof first === "number") {
			const n = Number(strValue);
			return Number.isNaN(n) ? undefined : n;
		}
	}

	return strValue;
}

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
		if (JSON.stringify(newParams) !== JSON.stringify(params.value)) {
			params.value = { ...newParams };
		}
	},
	{ deep: true },
);

const selectedTemplate = computed(() =>
	props.templates.find((t) => t.id === selectedTemplateId.value),
);

const schema = computed<JSONSchema | null>(() => {
	if (!selectedTemplate.value?.paramsSchema) return null;
	try {
		return JSON.parse(selectedTemplate.value.paramsSchema) as JSONSchema;
	} catch (e) {
		console.error("Failed to parse template schema", e);
		return null;
	}
});

// Reset params when template changes
watch(selectedTemplateId, (newId) => {
	if (newId) {
		const newParams: Record<string, ParamValue> = {};
		if (schema.value?.properties) {
			Object.entries(schema.value.properties).forEach(([key, prop]) => {
				if (prop.default !== undefined) {
					newParams[key] = prop.default as ParamValue;
				}
			});
		}
		params.value = newParams;
	}
});

// Emit params changes
watch(
	params,
	(newParams) => {
		if (JSON.stringify(newParams) !== JSON.stringify(props.modelValue.params)) {
			emit("update:modelValue", {
				templateId: selectedTemplateId.value,
				params: { ...newParams },
			});
		}
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

    <div v-if="schema?.properties" class="space-y-4 border-t pt-4">
      <h3 class="text-sm font-semibold">{{
          t('resume.pdfSelector.optionsTitle', 'Template Options')
        }}</h3>
      <div v-for="(prop, key) in schema.properties" :key="key" class="space-y-2">
        <!-- Enum Select -->
        <div v-if="hasEnum(prop)" class="space-y-2">
          <Label :for="String(key)">{{
              t(`resume.pdfSelector.param.${key}`, prop.description || String(key))
            }}</Label>
          <Select
              :model-value="toModelValue(params[key])"
              @update:model-value="(val: unknown) => (params[key] = fromModelValue(val, prop))"
          >
            <SelectTrigger :id="String(key)">
              <SelectValue
                  :placeholder="t(`resume.pdfSelector.param.${key}`, prop.description || String(key))"/>
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                  v-for="(opt, index) in prop.enum"
                  :key="index"
                  :value="String(opt)"
              >
                {{ opt }}
              </SelectItem>
            </SelectContent>
          </Select>
        </div>
        <!-- Boolean Checkbox -->
        <div v-else-if="isBooleanProperty(prop)" class="flex items-center space-x-2 py-2">
          <Checkbox
              :id="String(key)"
              :checked="Boolean(params[key])"
              @update:checked="(checked: boolean) => params[key] = checked"
          />
          <Label :for="String(key)" class="cursor-pointer">{{
              t(`resume.pdfSelector.param.${key}`, prop.description || String(key))
            }}</Label>
        </div>
        <!-- String/Number Input -->
        <div v-else class="space-y-2">
          <Label :for="String(key)">{{
              t(`resume.pdfSelector.param.${key}`, prop.description || String(key))
            }}</Label>
          <Input
              :id="String(key)"
              :model-value="toModelValue(params[key])"
              :type="isNumberProperty(prop) ? 'number' : 'text'"
              @update:model-value="(val: unknown) => (params[key] = fromModelValue(val, prop))"
          />
        </div>
      </div>
    </div>
  </div>
</template>
