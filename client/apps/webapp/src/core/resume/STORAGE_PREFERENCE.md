# User Storage Preference System

## Overview

This system allows users to choose where their resume data is stored (Session Storage, Local Storage, or IndexedDB). The preference is persisted across sessions and can be changed at any time with automatic data migration.

## Components

### 1. `useStoragePreference` Composable

Location: `src/core/resume/composables/useStoragePreference.ts`

Reactive composable that manages the user's storage preference:

```typescript
const {
  storagePreference,        // Current preference (reactive)
  setStoragePreference,     // Update preference
  availableStorageTypes,    // List of available types
  isStorageAvailable,       // Check if type is supported
  resetPreference           // Reset to default
} = useStoragePreference();
```

### 2. `StorageSelector` Component

Location: `src/components/StorageSelector.vue`

Visual UI for selecting storage type with:

- Radio button selection
- Storage type descriptions
- Capacity information
- Migration status feedback
- Error handling

### 3. Storage Factory

Location: `src/core/resume/infrastructure/storage/factory.ts`

Provides factory functions and metadata:

```typescript
// Create storage instance
const storage = createResumeStorage('local');

// Get default type
const defaultType = getDefaultStorageType();

// Get metadata for UI
const metadata = getStorageMetadata();
```

## User Flow

1. **Initial Load**
   - App reads preference from `localStorage.getItem('cvix:storage-preference')`
   - Creates appropriate storage instance
   - Provides it via dependency injection

2. **Viewing Settings**
   - User navigates to Settings page
   - `StorageSelector` component shows current selection
   - Displays available storage options with details

3. **Changing Storage**
   - User selects new storage type
   - Clicks "Apply & Migrate Data"
   - System:
     1. Creates new storage instance
     2. Saves current data to new storage
     3. Updates the active storage reference
     4. Persists preference to localStorage
     5. Shows success message

4. **Next Session**
   - App loads saved preference
   - Automatically uses preferred storage
   - Data is available from last session (if using persistent storage)

## Storage Options

### Session Storage (Default)

- **Persistence**: Tab/window session only
- **Capacity**: ~5-10 MB
- **Best for**: Privacy-conscious users, temporary work
- **Icon**: 🔒

### Local Storage

- **Persistence**: Permanent (across sessions)
- **Capacity**: ~5-10 MB
- **Best for**: Regular users, saving drafts
- **Icon**: 💾
- **Badge**: Recommended

### IndexedDB

- **Persistence**: Permanent (across sessions)
- **Capacity**: ~50+ MB
- **Best for**: Large resumes, power users
- **Icon**: 🗄️

## Implementation Details

### Preference Storage

The preference is stored in `localStorage` with the key:

```typescript
const STORAGE_PREFERENCE_KEY = "cvix:storage-preference";
```

Valid values: `"session" | "local" | "indexeddb" | "remote"`

### App Initialization

In `main.ts`:

```typescript
// Read user preference
const storagePreference = getUserStoragePreference();

// Create storage instance
const resumeStorage = createResumeStorage(storagePreference);

// Provide to app
app.provide(RESUME_STORAGE_KEY, resumeStorage);
```

### Store Integration

The store instance now has a mutable `currentStorage` ref that can be swapped at runtime:

```typescript
async function changeStorageStrategy(
  newStorage: ResumeStorage,
  migrateData = false
): Promise<void> {
  if (migrateData && resume.value) {
    await newStorage.save(resume.value);
  }
  currentStorage.value = newStorage;
  currentStorageType.value = newStorage.type();
}
```

## Usage Examples

### In a Component

```vue
<script setup lang="ts">
import StorageSelector from '@/components/StorageSelector.vue';
</script>

<template>
  <div>
    <h2>Choose Your Storage</h2>
    <StorageSelector />
  </div>
</template>
```

### Programmatically Change Storage

```typescript
import { useResumeStore } from '@/core/resume/infrastructure/store/resume.store';
import { createResumeStorage } from '@/core/resume/infrastructure/storage';

const resumeStore = useResumeStore();

// Change to local storage with migration
await resumeStore.changeStorageStrategy(
  createResumeStorage('local'),
  true // migrate existing data
);
```

### Check Current Storage

```typescript
import { useResumeStore } from '@/core/resume/infrastructure/store/resume.store';

const resumeStore = useResumeStore();
console.log(resumeStore.currentStorageType); // 'session' | 'local' | 'indexeddb'
```

## Testing

### Testing Storage Preference

```typescript
import { useStoragePreference } from '@/core/resume/composables/useStoragePreference';

// Mock localStorage
const mockStorage = {};
global.localStorage = {
  getItem: (key) => mockStorage[key],
  setItem: (key, value) => { mockStorage[key] = value; },
  // ...
};

const { storagePreference, setStoragePreference } = useStoragePreference();

// Test
setStoragePreference('local');
expect(localStorage.getItem('cvix:storage-preference')).toBe('local');
```

### Testing Storage Selector

```typescript
import { mount } from '@vue/test-utils';
import StorageSelector from '@/components/StorageSelector.vue';

const wrapper = mount(StorageSelector);

// Click local storage option
await wrapper.find('[value="local"]').trigger('click');

// Click apply button
await wrapper.find('button').trigger('click');

// Check that storage was changed
expect(wrapper.emitted()).toHaveProperty('storage-changed');
```

## Error Handling

The system handles various error scenarios:

1. **localStorage Not Available** (e.g., private browsing)
   - Falls back to session storage
   - Shows warning to user

2. **Migration Failure**
   - Displays error message
   - Keeps current storage
   - Doesn't update preference

3. **Storage Quota Exceeded**
   - Catches error during save
   - Shows informative message
   - Suggests switching to IndexedDB

## Accessibility

The `StorageSelector` component is fully accessible:

- Keyboard navigable
- Screen reader friendly labels
- ARIA attributes on radio group
- Focus management
- High contrast support

## Future Enhancements

1. **Remote Storage**
   - Add backend sync option
   - Implement when `remote` type is ready

2. **Auto Migration**
   - Detect when quota is exceeded
   - Suggest upgrading to larger storage

3. **Data Export**
   - Allow downloading data as backup
   - Before switching storage types

4. **Storage Analytics**
   - Show how much space is used
   - Display storage capacity warnings
