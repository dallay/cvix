# Component Contracts: PDF Section Selector

**Feature**: `005-pdf-section-selector`
**Date**: 2025-12-06

## Component Hierarchy

```text
ResumePdfPage.vue
├── DashboardLayout.vue (existing)
├── SectionTogglePanel.vue (NEW)
│   ├── SectionTogglePill.vue (NEW) × N sections
│   │   ├── ItemToggleList.vue (NEW) - for array sections [work, education, skills, ...]
│   │   └── PersonalDetailsFieldList.vue (NEW) - for Personal Details fields [email, phone, location, ...]
│   └── AddCustomSectionButton.vue (NEW - optional P2)
├── PdfTemplateSelector.vue (existing)
└── ResumePreview.vue (MODIFIED - uses filtered resume)
```

## Component Specifications

### 1. SectionTogglePanel.vue

**Purpose**: Container component for all section toggle pills

**Location**: `client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePanel.vue`

**Template Structure**:

```vue
<template>
  <div class="space-y-4">
    <div class="flex items-center justify-between">
      <h2 class="text-sm font-semibold text-muted-foreground">
        {{ $t('resume.pdfPage.visibleSections') }}
      </h2>
      <button
        class="inline-flex items-center gap-1 text-xs font-medium text-primary hover:text-primary/80"
      >
        <Plus class="h-3 w-3" />
        {{ $t('resume.pdfPage.addCustomSection') }}
      </button>
    </div>

    <div class="flex flex-wrap gap-2">
      <SectionTogglePill
        v-for="section in sections"
        :key="section.type"
        :label="$t(section.labelKey)"
        :enabled="getEnabled(section.type)"
        :has-data="section.hasData"
        :expanded="getExpanded(section.type)"
        :visible-count="section.visibleItemCount"
        :total-count="section.itemCount"
        @toggle="emit('toggle-section', section.type)"
        @expand="emit('expand-section', section.type)"
      >
        <ItemToggleList
          v-if="getExpanded(section.type) && section.type !== 'personalDetails'"
          :section-type="section.type"
          :items="getItems(section.type)"
          @toggle-item="(i) => emit('toggle-item', section.type, i)"
        />
        <PersonalDetailsFieldList
          v-else-if="getExpanded(section.type) && section.type === 'personalDetails'"
          :fields="visibility.personalDetails.fields"
          @toggle-field="(field) => emit('toggle-field', field)"
        />
      </SectionTogglePill>
    </div>
  </div>
</template>
```

**Props**:

```typescript
interface Props {
  resume: Resume;
  visibility: SectionVisibility;
}
```

**Emits**:

```typescript
interface Emits {
  (e: 'toggle-section', section: SectionType): void;
  (e: 'expand-section', section: SectionType): void;
  (e: 'toggle-item', section: ArraySectionType, index: number): void;
  (e: 'toggle-field', field: keyof PersonalDetailsFieldVisibility): void;
}
```

---

### 2. SectionTogglePill.vue

**Purpose**: Individual toggleable pill representing a resume section

**Location**: `client/apps/webapp/src/core/resume/infrastructure/presentation/components/SectionTogglePill.vue`

**Template Structure**:

```vue
<template>
  <div class="flex flex-col">
    <button
      :disabled="!hasData"
      :aria-checked="enabled"
      :aria-expanded="expanded"
      role="switch"
      :title="!hasData ? disabledTooltip : undefined"
      :class="[
        'inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition-all',
        'focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2',
        enabled
          ? 'bg-primary text-primary-foreground shadow-sm'
          : 'bg-background border border-input text-muted-foreground hover:bg-accent',
        !hasData && 'opacity-50 cursor-not-allowed',
      ]"
      @click="handleClick"
      @keydown.space.prevent="handleClick"
    >
      <!-- Checkmark indicator for enabled state -->
      <span
        v-if="enabled"
        class="flex h-4 w-4 items-center justify-center rounded-full bg-primary-foreground/20"
      >
        <Check class="h-2.5 w-2.5 text-primary-foreground" />
      </span>
      <span
        v-else
        class="flex h-4 w-4 items-center justify-center rounded-full bg-muted"
      />

      <span>{{ label }}</span>

      <!-- Item count badge -->
      <span
        v-if="totalCount !== undefined && totalCount > 0"
        :class="[
          'text-xs',
          enabled ? 'text-primary-foreground/70' : 'text-muted-foreground'
        ]"
      >
        {{ visibleCount }}/{{ totalCount }}
      </span>
    </button>

    <!-- Expandable item list slot -->
    <Collapsible :open="expanded" @update:open="$emit('expand')">
      <CollapsibleContent class="mt-2 ml-4">
        <slot />
      </CollapsibleContent>
    </Collapsible>
  </div>
</template>
```

**Props**:

```typescript
interface Props {
  label: string;
  enabled: boolean;
  hasData: boolean;
  expanded?: boolean;
  visibleCount?: number;
  totalCount?: number;
  disabledTooltip?: string;
}
```

**Emits**:

```typescript
interface Emits {
  (e: 'toggle'): void;
  (e: 'expand'): void;
}
```

**Behavior**:

- Click on pill toggles `enabled` state (emits `toggle`)
- If enabled, click expands/collapses item list (emits `expand`)
- Disabled pills show tooltip on hover
- Keyboard: Space/Enter activates toggle
- **Expansion state is parent-controlled**: Component is stateless for expansion; the parent (SectionTogglePanel) owns and manages the `expanded` state via prop, and responds to `expand` events to update state

---

### 3. ItemToggleList.vue

**Purpose**: Expandable list of toggleable items within a section

**Location**: `client/apps/webapp/src/core/resume/infrastructure/presentation/components/ItemToggleList.vue`

**Template Structure**:

```vue
<template>
  <div class="space-y-1 border-l-2 border-muted pl-3">
    <div
      v-for="(item, index) in items"
      :key="index"
      class="flex items-center gap-2"
    >
      <Checkbox
        :id="`${sectionType}-${index}`"
        :checked="item.enabled"
        @update:checked="emit('toggle-item', index)"
        class="h-4 w-4"
      />
      <label
        :for="`${sectionType}-${index}`"
        :class="[
          'text-sm cursor-pointer',
          item.enabled ? 'text-foreground' : 'text-muted-foreground line-through'
        ]"
      >
        {{ item.label }}
        <span v-if="item.sublabel" class="text-xs text-muted-foreground">
          · {{ item.sublabel }}
        </span>
      </label>
    </div>
  </div>
</template>
```

**Props**:

```typescript
interface Props {
  sectionType: ArraySectionType;
  items: Array<{
    label: string;
    sublabel?: string;
    enabled: boolean;
  }>;
}
```

**Emits**:

```typescript
interface Emits {
  (e: 'toggle-item', index: number): void;
}
```

---

### 4. PersonalDetailsFieldList.vue

**Purpose**: Specialized item list for Personal Details section fields

**Location**: `client/apps/webapp/src/core/resume/infrastructure/presentation/components/PersonalDetailsFieldList.vue`

**Template Structure**:

```vue
<template>
  <div class="space-y-1 border-l-2 border-muted pl-3">
    <!-- Name field - always visible, no checkbox -->
    <div class="flex items-center gap-2 text-sm text-muted-foreground">
      <Lock class="h-3 w-3" />
      <span>{{ $t('resume.fields.name') }}</span>
      <span class="text-xs italic">({{ $t('resume.pdfPage.alwaysVisible') }})</span>
    </div>

    <!-- Toggleable fields -->
    <div
      v-for="field in toggleableFields"
      :key="field.key"
      class="flex items-center gap-2"
    >
      <Checkbox
        :id="`personal-${field.key}`"
        :checked="fields[field.key]"
        @update:checked="emit('toggle-field', field.key)"
        class="h-4 w-4"
      />
      <label
        :for="`personal-${field.key}`"
        :class="[
          'text-sm cursor-pointer',
          fields[field.key] ? 'text-foreground' : 'text-muted-foreground line-through'
        ]"
      >
        {{ $t(field.labelKey) }}
      </label>
    </div>
  </div>
</template>
```

**Props**:

```typescript
interface Props {
  fields: PersonalDetailsFieldVisibility;
}

/**
 * toggleableFields is a computed property derived from the fields prop.
 * It is an array of objects: { key: keyof PersonalDetailsFieldVisibility, labelKey: string }
 * The component or parent must provide a mapping from field keys to i18n label keys.
 * Example:
 * const toggleableFields = [
 *   { key: 'email', labelKey: 'resume.fields.email' },
 *   { key: 'phone', labelKey: 'resume.fields.phone' },
 *   { key: 'location', labelKey: 'resume.fields.location' },
 *   { key: 'image', labelKey: 'resume.fields.image' },
 *   { key: 'summary', labelKey: 'resume.fields.summary' },
 *   { key: 'url', labelKey: 'resume.fields.url' },
 *   { key: 'profiles', labelKey: 'resume.fields.profiles' },
 * ];
 */
```

**Emits**:

```typescript
interface Emits {
  (e: 'toggle-field', field: keyof PersonalDetailsFieldVisibility): void;
}
```

---

## Styling Tokens

Based on Figma design analysis, these Tailwind classes align with the design system:

### Pill States

| State               | Classes                                                   |
| ------------------- | --------------------------------------------------------- |
| Enabled (active)    | `bg-primary text-primary-foreground shadow-sm`            |
| Disabled (inactive) | `bg-background border border-input text-muted-foreground` |
| Hover (inactive)    | `hover:bg-accent hover:text-accent-foreground`            |
| Focus               | `focus:ring-2 focus:ring-ring focus:ring-offset-2`        |
| No data             | `opacity-50 cursor-not-allowed`                           |

### Checkmark Icon Container

| State    | Classes                                                    |
| -------- | ---------------------------------------------------------- |
| Enabled  | `bg-primary-foreground/20` (semi-transparent white circle) |
| Disabled | `bg-muted` (gray circle, no checkmark)                     |

### Size & Spacing

| Element             | Value               |
| ------------------- | ------------------- |
| Pill height         | `h-[34px]` / `py-2` |
| Pill padding        | `px-4`              |
| Pill gap            | `gap-2`             |
| Border radius       | `rounded-full`      |
| Checkmark container | `h-4 w-4`           |
| Checkmark icon      | `h-2.5 w-2.5`       |

---

## Accessibility Requirements

### Keyboard Navigation

| Key        | Action                          |
| ---------- | ------------------------------- |
| Tab        | Move focus between pills        |
| Space      | Toggle section enabled state    |
| Enter      | Toggle section enabled state    |
| Arrow Down | When expanded, focus first item |
| Escape     | Collapse expanded section       |

### ARIA Attributes

```vue
<button
  role="switch"
  :aria-checked="enabled"
  :aria-expanded="expanded"
  :aria-disabled="!hasData"
  :aria-describedby="!hasData ? `${id}-tooltip` : undefined"
>
```

### Screen Reader Announcements

- Enabled pill: "Personal Details, checked, press space to toggle"
- Disabled pill: "Personal Details, not checked, press space to toggle"
- Expanded: "Personal Details, checked, expanded, 3 of 5 items visible"
- No data: "Projects, disabled, no data available"
