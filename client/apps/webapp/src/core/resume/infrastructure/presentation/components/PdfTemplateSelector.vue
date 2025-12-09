<script lang="ts" setup>
import {
	Carousel,
	CarouselContent,
	CarouselItem,
	CarouselNext,
	CarouselPrevious,
} from "@cvix/ui/components/ui/carousel";
import { Label } from "@cvix/ui/components/ui/label";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@cvix/ui/components/ui/select";
import { isEqual } from "@cvix/utilities";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
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

// Handle template card click
function onTemplateCardClick(templateId: string) {
	if (selectedTemplateId.value === templateId) {
		return; // Already selected
	}
	selectedTemplateId.value = templateId;
	const template = props.templates.find((t) => t.id === templateId);
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
    <!-- Template Cards Section -->
    <div class="space-y-3">
      <h3 class="text-sm font-semibold text-foreground">
        {{ t('resume.pdfSelector.templateLabel', 'Template') }}
      </h3>

      <!-- Loading State -->
      <div v-if="props.isLoading" class="flex justify-center py-8">
        <div class="text-sm text-muted-foreground">
          {{ t('resume.pdfSelector.loading', 'Loading templates…') }}
        </div>
      </div>

      <!-- Error State -->
      <div v-else-if="props.error" class="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
        {{ props.error }}
      </div>

      <!-- Empty State -->
      <div v-else-if="templates.length === 0" class="text-center py-8 text-sm text-muted-foreground">
        {{ t('resume.pdfSelector.noTemplates', 'No templates available') }}
      </div>

      <!-- Template Carousel -->
      <div v-else class="mx-8">
        <Carousel
          :opts="{
            align: 'start',
            loop: true,
          }"
          class="w-full"
        >
          <CarouselContent class="-ml-4">
            <CarouselItem
              v-for="template in templates"
              :key="template.id"
              class="pl-4 basis-full"
            >
              <button
                type="button"
                :class="[
                  'w-full h-full rounded-lg border-2 p-4 text-left transition-all duration-200 flex flex-col',
                  'hover:shadow-md hover:border-primary/50 focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2',
                  selectedTemplateId === template.id
                    ? 'border-primary bg-primary/5 shadow-sm'
                    : 'border-border bg-card hover:bg-accent/50'
                ]"
                :aria-label="t('resume.pdfSelector.selectTemplateAria', { name: template.name })"
                :aria-pressed="selectedTemplateId === template.id"
                @click="onTemplateCardClick(template.id)"
              >
                <div class="flex items-start gap-3 w-full">
                  <!-- Template Icon/Indicator -->
                  <div
                    :class="[
                      'flex h-10 w-10 shrink-0 items-center justify-center rounded-md text-sm font-semibold',
                      selectedTemplateId === template.id
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-muted text-muted-foreground'
                    ]"
                  >
                    {{ template.name.charAt(0).toUpperCase() }}
                  </div>

                  <!-- Template Info -->
                  <div class="flex-1 min-w-0">
                    <h4 :class="[
                      'text-sm font-semibold mb-1',
                      selectedTemplateId === template.id ? 'text-primary' : 'text-foreground'
                    ]">
                      {{ template.name }}
                    </h4>
                    <p v-if="template.description" class="text-xs text-muted-foreground line-clamp-2">
                      {{ template.description }}
                    </p>
                  </div>

                  <!-- Active Indicator -->
                  <div
                    v-if="selectedTemplateId === template.id"
                    class="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-primary"
                    data-testid="template-selected-indicator"
                  >
                    <svg class="h-3 w-3 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                    </svg>
                  </div>
                </div>
              </button>
            </CarouselItem>
          </CarouselContent>
          <CarouselPrevious />
          <CarouselNext />
        </Carousel>
      </div>
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
