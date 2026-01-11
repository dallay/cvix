<script lang="ts" setup>
/**
 * PdfTemplateSelector Component
 *
 * Template selection carousel with visual preview mockup and core options.
 * Design matches the Magic Patterns reference with:
 * - Template card with preview skeleton showing resume layout
 * - Letter icon badge (purple)
 * - Dot indicators for carousel position
 * - Core Options with icons (Globe, Type, Palette)
 */
import {
	Carousel,
	type CarouselApi,
	CarouselContent,
	CarouselItem,
	CarouselNext,
	CarouselPrevious,
} from "@cvix/ui/components/ui/carousel";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@cvix/ui/components/ui/select";
import { isEqual } from "@cvix/utilities";
import { Globe, Palette, Type } from "lucide-vue-next";
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

// Carousel state for dot indicators
const carouselApi = ref<CarouselApi>();
const currentSlide = ref(0);
const totalSlides = computed(() => props.templates.length);

// Watch carousel scroll to update current slide
watch(carouselApi, (api, oldApi) => {
	// Clean up previous listener if exists
	if (oldApi) {
		// Embla doesn't expose off(), but we can work around by replacing the api
		// In practice, carouselApi rarely changes, so this is mostly defensive
	}

	if (!api) return;

	// Named function for proper cleanup
	const handleSelect = () => {
		currentSlide.value = api.selectedScrollSnap();
	};

	api.on("select", handleSelect);

	// Return cleanup function
	return () => {
		if (api && typeof api.off === "function") {
			api.off("select", handleSelect);
		}
	};
});

const selectedTemplate = computed(() =>
	props.templates.find((t) => t.id === selectedTemplateId.value),
);

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

	// Set default font if not present (avoid non-null assertions)
	if (!newParams.fontFamily && SUPPORTED_FONTS.length > 0) {
		const [firstFont] = SUPPORTED_FONTS;
		newParams.fontFamily = firstFont;
	}

	// Set default color if not present (avoid non-null assertions)
	if (!newParams.colorPalette && SUPPORTED_COLORS.length > 0) {
		const [firstColor] = SUPPORTED_COLORS;
		newParams.colorPalette = firstColor;
	}

	return newParams;
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
		if (!isEqual(newParams, params.value)) {
			params.value = { ...newParams };
		}
	},
);

// Initialize params with defaults when template is selected but params are empty
// This handles the case when parent sets templateId but not params (e.g., on mount)
watch(
	[selectedTemplateId, () => props.templates],
	([templateId, templates]) => {
		if (!templateId || templates.length === 0) return;

		// Only initialize if params are empty (no user selections yet)
		const hasParams = Object.keys(params.value).length > 0;
		if (hasParams) return;

		const template = templates.find((t) => t.id === templateId);
		if (template) {
			params.value = buildTemplateParams(template);
		}
	},
	{ immediate: true },
);

// Handle template card click
function onTemplateCardClick(templateId: string) {
	if (selectedTemplateId.value === templateId) {
		return; // Already selected
	}
	// Defensive: ensure template exists before building params (avoid silent failure)
	const template = props.templates.find((t) => t.id === templateId);
	if (!template) {
		console.warn(
			`Template with id "${templateId}" not found in templates list`,
		);
		return;
	}
	selectedTemplateId.value = templateId;
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

// Navigate carousel to specific slide
function goToSlide(index: number) {
	if (carouselApi.value) {
		carouselApi.value.scrollTo(index);
	}
}
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
      <div v-else class="relative">
        <Carousel
          :opts="{
            align: 'start',
            loop: true,
          }"
          class="w-full"
          data-testid="template-carousel"
          @init-api="(api) => carouselApi = api"
        >
          <CarouselContent class="-ml-2">
            <CarouselItem
              v-for="template in templates"
              :key="template.id"
              class="pl-2 basis-full"
            >
              <button
                type="button"
                :class="[
                  'w-full rounded-xl border-2 p-4 text-left transition-all duration-200 relative',
                  'hover:shadow-md focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2',
                  selectedTemplateId === template.id
                    ? 'border-primary ring-1 ring-primary bg-primary/5 shadow-md'
                    : 'border-border bg-card hover:border-primary/50 hover:bg-accent/50'
                ]"
                :aria-label="t('resume.pdfSelector.selectTemplateAria', { name: template.name })"
                :aria-pressed="selectedTemplateId === template.id"
                @click="onTemplateCardClick(template.id)"
              >
                <!-- Corner Checkmark (selected state) -->
                <div
                  v-if="selectedTemplateId === template.id"
                  class="absolute top-2 right-2 flex h-6 w-6 items-center justify-center rounded-full bg-primary shadow-md z-10"
                  data-testid="template-corner-checkmark"
                >
                  <svg class="h-3.5 w-3.5 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                  </svg>
                </div>

                <div class="flex gap-4">
                  <!-- Template Preview Mockup (Skeleton) -->
                  <div
                    :class="[
                      'w-24 h-32 shrink-0 rounded-lg border p-2 flex flex-col gap-1.5',
                      selectedTemplateId === template.id
                        ? 'bg-background border-primary/30'
                        : 'bg-muted/50 border-border'
                    ]"
                  >
                    <!-- Header skeleton -->
                    <div class="flex items-center gap-1.5">
                      <div class="w-4 h-4 rounded-full bg-primary/30" />
                      <div class="flex-1 space-y-1">
                        <div class="h-1.5 bg-primary/40 rounded w-3/4" />
                        <div class="h-1 bg-muted-foreground/30 rounded w-1/2" />
                      </div>
                    </div>
                    <!-- Section divider -->
                    <div class="h-px bg-border my-1" />
                    <!-- Content lines -->
                    <div class="space-y-1 flex-1">
                      <div class="h-1 bg-muted-foreground/20 rounded w-full" />
                      <div class="h-1 bg-muted-foreground/20 rounded w-4/5" />
                      <div class="h-1 bg-muted-foreground/20 rounded w-3/4" />
                      <div class="h-px bg-border my-1.5" />
                      <div class="h-1 bg-muted-foreground/20 rounded w-full" />
                      <div class="h-1 bg-muted-foreground/20 rounded w-2/3" />
                    </div>
                  </div>

                  <!-- Template Info -->
                  <div class="flex-1 min-w-0 flex flex-col justify-between py-1">
                    <div>
                      <!-- Letter Badge + Name -->
                      <div class="flex items-center gap-2 mb-1">
                        <span
                          :class="[
                            'flex h-6 w-6 shrink-0 items-center justify-center rounded-md text-xs font-bold',
                            selectedTemplateId === template.id
                              ? 'bg-primary text-primary-foreground'
                              : 'bg-muted text-muted-foreground'
                          ]"
                        >
                          {{ template.name.charAt(0).toUpperCase() }}
                        </span>
                        <span
                          :class="[
                            'text-sm font-semibold truncate',
                            selectedTemplateId === template.id ? 'text-primary' : 'text-foreground'
                          ]"
                        >
                          {{ template.name }}
                        </span>
                      </div>

                      <!-- Description -->
                      <p
                        v-if="template.description"
                        class="text-xs text-muted-foreground line-clamp-2 leading-relaxed"
                      >
                        {{ template.description }}
                      </p>
                    </div>

                    <!-- Selected Indicator -->
                    <div
                      v-if="selectedTemplateId === template.id"
                      class="flex items-center gap-1.5 mt-2"
                      data-testid="template-selected-indicator"
                    >
                      <span class="flex h-4 w-4 items-center justify-center rounded-full bg-primary">
                        <svg class="h-2.5 w-2.5 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                        </svg>
                      </span>
                      <span class="text-xs text-primary font-medium">
                        {{ t('resume.pdfSelector.selected', 'Selected') }}
                      </span>
                    </div>
                  </div>
                </div>
              </button>
            </CarouselItem>
          </CarouselContent>

          <!-- Navigation Arrows -->
          <CarouselPrevious class="left-0 -translate-x-1/2" />
          <CarouselNext class="right-0 translate-x-1/2" />
        </Carousel>

        <!-- Dot Indicators -->
        <div v-if="totalSlides > 1" class="flex justify-center gap-1.5 mt-3">
          <button
            v-for="(_, index) in templates"
            :key="index"
            type="button"
            :class="[
              'w-2 h-2 rounded-full transition-all duration-200',
              currentSlide === index
                ? 'bg-primary w-4'
                : 'bg-muted-foreground/30 hover:bg-muted-foreground/50'
            ]"
            :aria-label="`Go to template ${index + 1}`"
            @click="goToSlide(index)"
          />
        </div>
      </div>
    </div>

    <!-- Core Options Section -->
    <div v-if="selectedTemplate" class="space-y-3 border-t border-border pt-4">
      <h3 class="text-sm font-semibold text-foreground">
        {{ t('resume.pdfSelector.coreOptions', 'Core Options') }}
      </h3>

      <div class="space-y-2">
        <!-- Locale -->
        <div
          v-if="selectedTemplate.supportedLocales?.length"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-secondary/50 hover:bg-secondary/80 transition-colors"
          data-testid="locale-option-row"
        >
          <Globe class="h-4 w-4 text-muted-foreground shrink-0" />
          <Select
            :model-value="getParamString('locale')"
            @update:model-value="(val) => updateParam('locale', val)"
          >
            <SelectTrigger
              class="flex-1 border-0 bg-transparent shadow-none h-auto p-0 focus:ring-0"
              aria-label="Resume language selector"
            >
              <SelectValue :placeholder="t('resume.pdfSelector.selectLocale', 'Select language')" />
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
        <div
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-secondary/50 hover:bg-secondary/80 transition-colors"
          data-testid="font-option-row"
        >
          <Type class="h-4 w-4 text-muted-foreground shrink-0" />
          <Select
            :model-value="getParamString('fontFamily')"
            @update:model-value="(val) => updateParam('fontFamily', val)"
          >
            <SelectTrigger
              class="flex-1 border-0 bg-transparent shadow-none h-auto p-0 focus:ring-0"
              aria-label="Resume font selector"
            >
              <SelectValue :placeholder="t('resume.pdfSelector.selectFont', 'Select font')" />
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
        <div
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg bg-secondary/50 hover:bg-secondary/80 transition-colors"
          data-testid="color-option-row"
        >
          <Palette class="h-4 w-4 text-muted-foreground shrink-0" />
          <Select
            :model-value="getParamString('colorPalette')"
            @update:model-value="(val) => updateParam('colorPalette', val)"
          >
            <SelectTrigger
              class="flex-1 border-0 bg-transparent shadow-none h-auto p-0 focus:ring-0"
              aria-label="Resume color selector"
            >
              <SelectValue :placeholder="t('resume.pdfSelector.selectColor', 'Select color')" />
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
  </div>
</template>
