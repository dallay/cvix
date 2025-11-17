# Storage Configuration Examples

This document demonstrates different ways to configure the storage strategy for the resume persistence system.

## Default Configuration (No Setup Required)

If you don't provide any storage configuration, the system automatically uses `SessionStorageResumeStorage`:

```typescript
// In any component
const resumeStore = useResumeStore();
await resume.store.saveToStorage(); // Uses session storage by default
```

## Global Configuration via Dependency Injection

Configure storage once in your app initialization and make it available everywhere:

### Session Storage (Default)

```typescript
// main.ts
import { createApp } from 'vue';
import App from './App.vue';
import { RESUME_STORAGE_KEY } from '@/core/resume/infrastructure/di';
import { SessionStorageResumeStorage } from '@/core/resume/infrastructure/storage';

const app = createApp(App);
app.provide(RESUME_STORAGE_KEY, new SessionStorageResumeStorage());
app.mount('#app');
```

### Local Storage (Persistent)

```typescript
// main.ts
import { LocalStorageResumeStorage } from '@/core/resume/infrastructure/storage';

app.provide(RESUME_STORAGE_KEY, new LocalStorageResumeStorage());
```

### IndexedDB (Large Data)

```typescript
// main.ts
import { IndexedDBResumeStorage } from '@/core/resume/infrastructure/storage';

app.provide(RESUME_STORAGE_KEY, new IndexedDBResumeStorage());
```

## User Preference Based Configuration

Allow users to choose their preferred storage method:

```typescript
// main.ts
import {
  SessionStorageResumeStorage,
  LocalStorageResumeStorage,
  IndexedDBResumeStorage,
} from '@/core/resume/infrastructure/storage';

type StoragePreference = 'session' | 'local' | 'indexeddb';

function getUserStoragePreference(): StoragePreference {
  const preference = localStorage.getItem('cvix:storage-preference');
  return (preference as StoragePreference) ?? 'session';
}

function createStorageFromPreference(preference: StoragePreference) {
  switch (preference) {
    case 'local':
      return new LocalStorageResumeStorage();
    case 'indexeddb':
      return new IndexedDBResumeStorage();
    default:
      return new SessionStorageResumeStorage();
  }
}

const userPreference = getUserStoragePreference();
const storage = createStorageFromPreference(userPreference);
app.provide(RESUME_STORAGE_KEY, storage);
```

## Runtime Strategy Switching

Switch storage strategies dynamically based on user actions:

```typescript
// In a settings component
import { useResumeStore } from '@/core/resume/infrastructure/store/resume.store';
import { LocalStorageResumeStorage } from '@/core/resume/infrastructure/storage';

async function upgradeToLocalStorage() {
  const resumeStore = useResumeStore();

  // Switch to local storage and migrate existing data
  await resume.store.changeStorageStrategy(
    new LocalStorageResumeStorage(),
    true // migrate data from current storage
  );

  // Save preference for next session
  localStorage.setItem('cvix:storage-preference', 'local');
}
```

## Feature Flag Based Configuration

Control storage behavior using feature flags:

```typescript
// main.ts
interface FeatureFlags {
  useIndexedDB: boolean;
  enableOfflineMode: boolean;
}

const featureFlags: FeatureFlags = {
  useIndexedDB: import.meta.env.VITE_USE_INDEXEDDB === 'true',
  enableOfflineMode: import.meta.env.VITE_OFFLINE_MODE === 'true',
};

function configureStorage(flags: FeatureFlags) {
  if (flags.useIndexedDB) {
    return new IndexedDBResumeStorage();
  }

  if (flags.enableOfflineMode) {
    return new LocalStorageResumeStorage();
  }

  return new SessionStorageResumeStorage();
}

const storage = configureStorage(featureFlags);
app.provide(RESUME_STORAGE_KEY, storage);
```

## Storage Migration Example

Migrate data from one storage to another:

```typescript
// Migration utility
import type { ResumeStorage } from '@/core/resume/domain/ResumeStorage';

async function migrateStorage(
  fromStorage: ResumeStorage,
  toStorage: ResumeStorage
) {
  try {
    // Load from old storage
    const result = await fromStorage.load();

    if (result.data) {
      // Save to new storage
      await toStorage.save(result.data);

      // Clear old storage
      await fromStorage.clear();

      console.log('Migration successful');
    }
  } catch (error) {
    console.error('Migration failed:', error);
    throw error;
  }
}

// Usage
import { SessionStorageResumeStorage, LocalStorageResumeStorage } from '@/core/resume/infrastructure/storage';

await migrateStorage(
  new SessionStorageResumeStorage(),
  new LocalStorageResumeStorage()
);
```

## Settings Component Example

Complete example of a settings component with storage selection:

```vue
<script setup lang="ts">
import { ref } from 'vue';
import { useResumeStore } from '@/core/resume/infrastructure/store/resume.store';
import {
  SessionStorageResumeStorage,
  LocalStorageResumeStorage,
  IndexedDBResumeStorage,
} from '@/core/resume/infrastructure/storage';
import type { StorageType } from '@/core/resume/domain/ResumeStorage';

const resumeStore = useResumeStore();
const selectedStorage = ref<StorageType>(resume.store.currentStorageType);
const isMigrating = ref(false);

async function changeStorage(newType: StorageType, migrate: boolean) {
  isMigrating.value = true;

  try {
    let storage;
    switch (newType) {
      case 'local':
        storage = new LocalStorageResumeStorage();
        break;
      case 'indexeddb':
        storage = new IndexedDBResumeStorage();
        break;
      default:
        storage = new SessionStorageResumeStorage();
    }

    await resume.store.changeStorageStrategy(storage, migrate);
    selectedStorage.value = newType;

    // Persist preference
    localStorage.setItem('cvix:storage-preference', newType);
  } catch (error) {
    console.error('Failed to change storage:', error);
  } finally {
    isMigrating.value = false;
  }
}
</script>

<template>
  <div class="storage-settings">
    <h3>Storage Preferences</h3>

    <select v-model="selectedStorage" :disabled="isMigrating">
      <option value="session">Session (Temporary)</option>
      <option value="local">Local (Persistent)</option>
      <option value="indexeddb">IndexedDB (Advanced)</option>
    </select>

    <button
      @click="changeStorage(selectedStorage, true)"
      :disabled="isMigrating"
    >
      {{ isMigrating ? 'Migrating...' : 'Save & Migrate Data' }}
    </button>
  </div>
</template>
```

## Environment-Based Configuration

Configure storage based on environment:

```typescript
// config/storage.ts
import type { ResumeStorage } from '@/core/resume/domain/ResumeStorage';
import {
  SessionStorageResumeStorage,
  LocalStorageResumeStorage,
  IndexedDBResumeStorage,
} from '@/core/resume/infrastructure/storage';

export function getDefaultStorage(): ResumeStorage {
  const env = import.meta.env.MODE;

  switch (env) {
    case 'production':
      return new LocalStorageResumeStorage();
    case 'development':
      return new SessionStorageResumeStorage();
    case 'test':
      return new SessionStorageResumeStorage();
    default:
      return new SessionStorageResumeStorage();
  }
}

// main.ts
import { getDefaultStorage } from './config/storage';

app.provide(RESUME_STORAGE_KEY, getDefaultStorage());
```
