# Resume Form - Presentation Layer Integration

## Overview

This document explains how the presentation layer (`ResumeForm.vue`) is connected to the domain and infrastructure layers through the `useResumeForm` composable.

## Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ResumeForm.vue                                        │  │
│  │   - Handles user interactions                         │  │
│  │   - Displays form fields                              │  │
│  │   - Shows validation errors                           │  │
│  └───────────────────┬───────────────────────────────────┘  │
│                      │ uses                                 │
│  ┌───────────────────▼───────────────────────────────────┐  │
│  │ useResumeForm() composable                            │  │
│  │   - Manages form state                                │  │
│  │   - Provides reactive fields                          │  │
│  │   - Converts readonly to mutable arrays               │  │
│  └───────────────────┬───────────────────────────────────┘  │
└────────────────────┬─┼────────────────────────────────────┘
                     │ │
┌────────────────────▼─▼────────────────────────────────────┐
│              INFRASTRUCTURE LAYER                          │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ resumeStore (Pinia)                                   │ │
│  │   - Validates resume data                             │ │
│  │   - Generates PDFs                                    │ │
│  │   - Manages generation state                          │ │
│  └───────────────────┬───────────────────────────────────┘ │
│                      │ uses (via DI)                        │
│  ┌───────────────────▼───────────────────────────────────┐ │
│  │ JsonResumeValidator                                   │ │
│  │ ResumeHttpClient                                      │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## useResumeForm Composable

### Purpose

The `useResumeForm` composable is the bridge between the presentation layer and the infrastructure layer. It:

1. **Provides mutable form fields** - Converts readonly domain types to mutable refs
2. **Auto-validates** - Watches form changes and validates automatically
3. **Handles submission** - Validates and saves resume data
4. **Generates PDFs** - Calls the resume generator
5. **Manages state** - Tracks loading, validation, and errors

### Usage in ResumeForm.vue

```typescript
import { useResumeForm } from '@/core/resume/infrastructure/presentation/composables/useResumeForm'

const {
  // Form fields (mutable)
  basics,
  workExperiences,
  volunteers,
  // ... other sections

  // Computed state
  isValid,
  isGenerating,
  generationError,

  // Actions
  submitResume,
  generatePdf,
  clearForm,
} = useResumeForm()
```

### Form Fields

All form fields are reactive `ref` objects that can be mutated:

```typescript
// basics is ref<MutableBasics>
basics.value.name = "John Doe"
basics.value.email = "john@example.com"
basics.value.profiles.push({
  network: "LinkedIn",
  username: "johndoe",
  url: "https://linkedin.com/in/johndoe"
})

// workExperiences is ref<Work[]>
workExperiences.value.push({
  name: "Tech Corp",
  position: "Developer",
  // ...
})
```

### Auto-validation

The composable watches all form fields and automatically validates when they change:

```typescript
// In useResumeForm.ts
watch(
  resume,
  (newResume) => {
    resumeStore.setResume(newResume)
  },
  { deep: true }
)
```

This means:

- `isValid` is always up-to-date
- The store always has the latest resume data
- No manual validation calls needed

### Actions

#### submitResume()

Validates the current resume and returns the validation result.

```typescript
async function handleSubmit(event: Event) {
  event.preventDefault()

  const valid = submitResume()

  if (!valid) {
    toast.error("Validation Error", {
      description: "Please check all required fields"
    })
    return
  }

  toast.success("Resume saved successfully!")
}
```

#### generatePdf(locale?)

Generates a PDF from the current resume data.

```typescript
async function handleGeneratePdf() {
  try {
    const pdfBlob = await generatePdf("en")

    // Create download link
    const url = URL.createObjectURL(pdfBlob)
    const link = document.createElement("a")
    link.href = url
    link.download = "resume.pdf"
    link.click()
    URL.revokeObjectURL(url)

    toast.success("PDF Generated!")
  } catch (error) {
    toast.error("Generation Failed", {
      description: generationError.value?.detail
    })
  }
}
```

#### clearForm()

Resets all form fields to their initial state.

```typescript
function handleCancel() {
  if (confirm("Clear all form data?")) {
    clearForm()
    toast.success("Form cleared")
  }
}
```

## Component Template Integration

### Connecting Form Fields

Each section component receives its data via `v-model`:

```vue
<template>
  <form @submit="handleSubmit">
    <FieldGroup>
      <!-- Basics section (no v-model needed, uses inject internally) -->
      <BasicsSection />

      <!-- Profiles -->
      <ProfilesField v-model="basics.profiles" />

      <!-- Work Experience -->
      <WorkExperienceSection v-model="workExperiences" />

      <!-- Other sections... -->
    </FieldGroup>
  </form>
</template>
```

### Button States

Buttons are disabled based on the composable's state:

```vue
<Button
  type="submit"
  :disabled="isSubmitting || isGenerating"
>
  {{ isSubmitting ? 'Saving...' : 'Submit' }}
</Button>

<Button
  variant="outline"
  :disabled="!isValid || isGenerating"
  @click="handleGeneratePdf"
>
  {{ isGenerating ? 'Generating...' : 'Generate PDF' }}
</Button>
```

### Error Display

Show generation errors to the user:

```vue
<div v-if="generationError" class="error">
  <p>{{ generationError.title }}</p>
  <p>{{ generationError.detail }}</p>
</div>
```

## Data Flow

### Form Input → Store → Validation

```text
User types in input
       ↓
basics.value.name changes
       ↓
watch() triggers
       ↓
resumeStore.setResume(resume)
       ↓
Validator validates resume
       ↓
isValid computed updates
       ↓
UI reflects validation state
```

### Submit → Save

```text
User clicks Submit
       ↓
handleSubmit() called
       ↓
submitResume() validates
       ↓
Store has latest data
       ↓
Show success/error toast
```

### Generate PDF

```text
User clicks Generate PDF
       ↓
handleGeneratePdf() called
       ↓
generatePdf() called on composable
       ↓
resumeStore.generatePdf() called
       ↓
ResumeHttpClient.generatePdf() (via DI)
       ↓
HTTP request to backend
       ↓
PDF blob returned
       ↓
Download link created
       ↓
User downloads PDF
```

## Type Safety

### Readonly vs Mutable

The domain uses `ReadonlyArray` to ensure immutability:

```typescript
// Domain (readonly)
interface Resume {
  basics: Basics
  work: ReadonlyArray<Work>
}

interface Basics {
  profiles: ReadonlyArray<Profile>
}
```

The composable converts these to mutable arrays for form editing:

```typescript
// Form (mutable)
interface MutableBasics extends Omit<Basics, 'profiles'> {
  profiles: Profile[]
}

const basics = ref<MutableBasics>({
  // ...
  profiles: [] // Mutable array
})
```

When loading data:

```typescript
function loadResume(resumeData: Resume): void {
  basics.value = {
    ...resumeData.basics,
    profiles: [...resumeData.basics.profiles] // Convert to mutable
  }
}
```

## Testing

The composable has comprehensive test coverage (11 tests):

- ✅ Initialization with empty data
- ✅ Form state updates and auto-validation
- ✅ Resume submission and validation
- ✅ PDF generation (success and error cases)
- ✅ Form clearing
- ✅ Resume loading with readonly→mutable conversion

See `useResumeForm.test.ts` for test examples.

## Best Practices

### 1. Always use the composable

❌ **Don't** access the store directly from components:

```vue
<script setup>
import { useResumeStore } from '@/core/resume/infrastructure/store/resumeStore'

const store = useResumeStore() // ❌ Don't do this
</script>
```

✅ **Do** use the composable:

```vue
<script setup>
import { useResumeForm } from '@/core/resume/infrastructure/presentation/composables/useResumeForm'

const { basics, submitResume } = useResumeForm() // ✅ Correct
</script>
```

### 2. Let auto-validation work

❌ **Don't** manually call validation after every change:

```typescript
basics.value.name = "John"
submitResume() // ❌ Unnecessary
```

✅ **Do** rely on auto-validation:

```typescript
basics.value.name = "John"
// isValid automatically updates via watch()
console.log(isValid.value) // ✅ Always current
```

### 3. Use the provided actions

✅ Use composable actions for form operations:

```typescript
const { submitResume, generatePdf, clearForm } = useResumeForm()

// Submit
handleSubmit() => submitResume()

// Generate PDF
handleGeneratePdf() => generatePdf("en")

// Clear
handleCancel() => clearForm()
```

### 4. Handle errors properly

✅ Always handle errors from async operations:

```typescript
try {
  const pdf = await generatePdf("en")
  // Success handling
} catch (error) {
  // Check composable's generationError for details
  if (generationError.value) {
    console.error(generationError.value.detail)
  }
}
```

## Future Enhancements

Potential improvements:

1. **Field-level validation** - Validate individual fields on blur
2. **Dirty state tracking** - Track which fields have been modified
3. **Auto-save** - Debounced auto-save to backend
4. **Undo/Redo** - Form history management
5. **Multi-language support** - i18n for error messages
6. **Progress indicator** - Show completion percentage

## Related Documentation

- [DI Architecture](../DI_ARCHITECTURE.md) - Dependency Injection patterns
- [Store README](../store/README.md) - Resume store documentation
- [Validator README](../validation/README.md) - JSON Resume validation
