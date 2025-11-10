# Settings Feature

This module implements the user settings feature following the hexagonal architecture pattern.

## Overview

The settings feature manages user preferences and configuration, including:

- Storage preference (where to persist resume data: session, local, IndexedDB, or remote)
- Theme preference (light, dark, or system)
- Language preference
- Notification settings

## Architecture

The feature follows the hexagonal architecture (ports and adapters) pattern:

```text
settings/
├── domain/                      # Domain layer (pure business logic)
│   ├── UserSettings.ts          # Settings entity and value objects
│   ├── SettingsRepository.ts    # Repository interface (port)
│   └── index.ts
├── application/                 # Application layer (use cases)
│   ├── updateStoragePreference.ts
│   └── index.ts
├── infrastructure/              # Infrastructure layer (adapters)
│   ├── di/                      # Dependency injection
│   │   ├── keys.ts
│   │   └── index.ts
│   ├── storage/                 # Storage adapters
│   │   ├── LocalStorageSettingsRepository.ts
│   │   ├── __tests__/
│   │   └── index.ts
│   ├── store/                   # Pinia store
│   │   ├── settingsStore.ts
│   │   └── index.ts
│   └── index.ts
├── presentation/                # Presentation layer (UI)
│   ├── components/
│   │   └── StorageSelector.vue
│   ├── composables/
│   │   ├── useStoragePreference.ts
│   │   └── index.ts
│   └── index.ts
└── index.ts                     # Public API
```

### Layer Responsibilities

#### Domain Layer

- Defines the core business entities (`UserSettings`)
- Defines value objects (`StoragePreference`, `ThemePreference`)
- Defines repository interfaces (ports)
- Contains validation logic
- **Zero dependencies** on frameworks or infrastructure

#### Application Layer

- Implements use cases (business logic orchestration)
- Coordinates domain objects and repository operations
- Independent of UI and infrastructure concerns

#### Infrastructure Layer

- Implements repository interfaces (adapters)
- Manages persistence (localStorage)
- Provides the Pinia store for state management
- Handles dependency injection

#### Presentation Layer

- Contains Vue components
- Provides composables for UI logic
- Manages user interactions

## Usage

### Basic Usage

```typescript
import { useSettingsStore } from '@/core/settings';

const settingsStore = useSettingsStore();

// Load settings (typically done in App.vue on mount)
await settingsStore.loadSettings();

// Update storage preference
await settingsStore.updateStoragePreference('local');

// Access current settings
console.log(settingsStore.settings.storagePreference); // 'local'
```

### Using the Storage Preference Composable

```typescript
import { useStoragePreference } from '@/core/settings';

const {
  storagePreference,
  setStoragePreference,
  availableStorageTypes,
  isStorageAvailable
} = useStoragePreference();

// Get current preference
console.log(storagePreference.value); // 'session' | 'local' | 'indexeddb' | 'remote'

// Change preference
await setStoragePreference('local');

// Check if a storage type is available
if (isStorageAvailable('indexeddb')) {
  console.log('IndexedDB is supported');
}
```

### Using the StorageSelector Component

```vue
<script setup lang="ts">
import { StorageSelector } from '@/core/settings';
</script>

<template>
  <StorageSelector />
</template>
```

## Dependency Injection

The settings feature uses Vue's provide/inject system for dependency injection:

```typescript
// In main.ts
import { LocalStorageSettingsRepository, SETTINGS_REPOSITORY_KEY } from '@/core/settings';

const settingsRepository = new LocalStorageSettingsRepository();
app.provide(SETTINGS_REPOSITORY_KEY, settingsRepository);
```

The store automatically uses the injected repository or falls back to `LocalStorageSettingsRepository`.

## Storage

Settings are persisted to `localStorage` under the key `cvix:user-settings` in JSON format:

```json
{
  "storagePreference": "local",
  "theme": "dark",
  "language": "en",
  "notifications": {
    "enabled": true,
    "email": true,
    "push": false
  }
}
```

## Integration with Resume Storage

The settings feature integrates with the resume storage system:

1. User selects a storage preference in the Settings page
2. The preference is saved to the settings store
3. The main.ts reads the preference and configures the resume storage accordingly
4. The resume store can dynamically change storage based on the preference

## Testing

The feature includes unit tests for the repository implementation:

```bash
pnpm test -- src/core/settings/infrastructure/storage/__tests__/
```

## Adding New Settings

To add a new setting:

1. Update `UserSettings` interface in `domain/UserSettings.ts`
2. Update `DEFAULT_USER_SETTINGS` with the default value
3. Add a use case in `application/` if needed
4. Update the store with new actions if needed
5. Add UI components in `presentation/components/`

## Best Practices

1. **Always use the store** for state management, not direct repository access
2. **Load settings on app mount** to ensure preferences are available
3. **Validate input** in the domain layer before persisting
4. **Handle errors gracefully** - the store provides error state
5. **Test all layers** - unit test repositories, integration test the store

## Migration Notes

This feature replaces the old `useStoragePreference` composable that was in the resume feature. The storage preference is now managed centrally in the settings feature, allowing for:

- Better separation of concerns
- Extensibility for additional settings
- Consistent persistence across the application
- Proper hexagonal architecture
