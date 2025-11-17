# Resume Persistence System

## Overview

The resume persistence system implements a flexible, storage-agnostic architecture for saving and
loading resume data. It follows the **Strategy Pattern** to allow switching between different
storage mechanisms without changing application code.

## Architecture

### Domain Layer (`domain/`)

- **`ResumeStorage.ts`**: Core interfaces and types
    - `ResumeStorage`: Main interface for all storage implementations
    - `StorageType`: Type union for supported storage types
    - `PersistenceResult<T>`: Standardized result object with metadata
    - `PartialResume`: Utility type for draft/incomplete resumes

### Application Layer (`application/`)

- **`ResumePersistenceService.ts`**: Service that uses the strategy pattern
    - Delegates operations to the configured storage strategy
    - Provides runtime strategy switching
    - Storage-agnostic API

### Infrastructure Layer (`infrastructure/storage/`)

Three browser storage implementations:

1. **`SessionStorageResumeStorage`** (default)
    - Uses `sessionStorage` API
    - Data cleared when browser tab closes
    - Best for privacy-conscious users

2. **`LocalStorageResumeStorage`**
    - Uses `localStorage` API
    - Persists across browser sessions
    - Best for long-term draft editing

3. **`IndexedDBResumeStorage`**
    - Uses IndexedDB API
    - Asynchronous, structured storage
    - Best for large resumes or multiple versions

## Usage

### In Components

The easiest way to use persistence is through the Pinia store:

```typescript
import {useResumeStore} from '@/core/resume/infrastructure/store/resume.store';

// In a component
const resumeStore = useResumeStore();

// Save current resume
await resumeStore.saveToStorage();

// Load from storage
await resumeStore.loadFromStorage();

// Clear storage
await resumeStore.clearStorage();

// Check storage type
console.log(resumeStore.currentStorageType); // 'session' | 'local' | 'indexeddb'
```

### Direct Service Usage

For more control, use the service directly:

```typescript
import {ResumePersistenceService} from '@/core/resume/application/ResumePersistenceService';
import {LocalStorageResumeStorage} from '@/core/resume/infrastructure/storage';

// Create service with local storage
const service = new ResumePersistenceService(new LocalStorageResumeStorage());

// Save
await service.save(myResume);

// Load
const result = await service.load();
if (result.data) {
    console.log('Loaded resume:', result.data);
}

// Clear
await service.clear();
```

### Changing Storage Strategy

```typescript
import {SessionStorageResumeStorage} from '@/core/resume/infrastructure/storage';

const resumeStore = useResumeStore();

// Load from current storage
const result = await resumeStore.loadFromStorage();

// Change to session storage and migrate data
await resumeStore.changeStorageStrategy(
        new SessionStorageResumeStorage(),
        true // migrate existing data
);
```

### Dependency Injection

Configure the storage strategy globally using Vue's provide/inject:

```typescript
// In main.ts or app setup
import {RESUME_STORAGE_KEY} from '@/core/resume/infrastructure/di';
import {LocalStorageResumeStorage} from '@/core/resume/infrastructure/storage';

app.provide(RESUME_STORAGE_KEY, new LocalStorageResumeStorage());
```

## Storage Comparison

| Feature     | Session Storage  | Local Storage    | IndexedDB          |
|-------------|------------------|------------------|--------------------|
| Persistence | Tab session only | Across sessions  | Across sessions    |
| Capacity    | ~5-10 MB         | ~5-10 MB         | ~50+ MB            |
| Performance | Sync, fast       | Sync, fast       | Async, scalable    |
| Complexity  | Simple           | Simple           | Moderate           |
| Use Case    | Temporary drafts | Long-term drafts | Large/complex data |

## Type Safety

All operations are fully typed using TypeScript:

```typescript
// Save accepts Resume or PartialResume
const result: PersistenceResult<Resume | PartialResume> = await service.save(resume);

// Load returns Resume or null
const loadResult: PersistenceResult<Resume | null> = await service.load();

// Clear returns void
await service.clear();
```

## Error Handling

All storage operations can throw errors. Always use try-catch:

```typescript
const resumeStore = useResumeStore();

try {
    await resumeStore.saveToStorage();
} catch (error) {
    console.error('Failed to save:', resumeStore.storageError);
}
```

## Future: Remote Storage

The architecture is designed to support remote backend storage:

```typescript
// Future implementation
export class RemoteResumeStorage implements ResumeStorage {
    async save(resume: Resume | PartialResume) {
        // PUT /api/resume/{uuid}
    }

    async load() {
        // GET /api/resume/{uuid}
    }

    type(): StorageType {
        return 'remote';
    }
}
```

## Testing

All storage implementations follow the same interface, making testing straightforward:

```typescript
// Mock storage for testing
class MockResumeStorage implements ResumeStorage {
    private data: Resume | null = null;

    async save(resume: Resume) {
        this.data = resume;
        return {data: resume, timestamp: new Date().toISOString(), storageType: 'session'};
    }

    async load() {
        return {data: this.data, timestamp: new Date().toISOString(), storageType: 'session'};
    }

    async clear() {
        this.data = null;
    }

    type() {
        return 'session' as const;
    }
}
```

## Best Practices

1. **Always handle errors**: Storage operations can fail (quota exceeded, permissions, etc.)
2. **Validate before saving**: Use the validator to ensure data integrity
3. **Use partial resumes for drafts**: Don't require all fields to be present
4. **Clear storage on logout**: Protect user privacy
5. **Test storage availability**: Check if storage APIs are available before use

## References

- [JSON Resume Schema](https://jsonresume.org/schema/)
- [Web Storage API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API)
- [IndexedDB API](https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API)
