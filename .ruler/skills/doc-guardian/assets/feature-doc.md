---
title: {Feature Name}
description: {One-line description of what this feature does}
last_updated: {YYYY-MM-DD}
---

# {Feature Name}

{Brief overview - what problem does this solve?}

## Overview

{More detailed explanation of the feature}

## Usage

### Basic Example

```typescript
// Real, working code example
import { featureFunction } from "@cvix/package";

const result = featureFunction({
 param1: "value",
 param2: 42,
});

console.log(result); // Expected output
```

### Advanced Example

Complex usage with error handling and configuration:

- **Error handling**: Wrap calls in try/catch to handle specific error types
- **Advanced configuration**: Pass additional options for customization
- **Edge case handling**: Account for empty inputs, timeouts, and boundary conditions

```typescript
import { featureFunction, FeatureError, ValidationError, TimeoutError } from '@cvix/feature';

try {
  const result = await featureFunction({
    param1: 'value',
    param2: 42,
    // Advanced options
    timeout: 5000,
    retries: 3,
    onProgress: (percent) => console.log(`Progress: ${percent}%`),
  });

  // Handle success
  console.log('Result:', result);
} catch (error) {
  if (error instanceof ValidationError) {
    // Handle validation failures (code: 'INVALID_PARAM')
    console.error('Validation failed:', error.field, error.message);
  } else if (error instanceof TimeoutError) {
    // Handle timeout (code: 'TIMEOUT')
    console.error('Operation timed out after', error.duration, 'ms');
  } else if (error instanceof FeatureError) {
    // Handle other feature-specific errors
    console.error('Feature error:', error.code, error.message);
  } else {
    throw error; // Re-throw unexpected errors
  }
}
```

## API Reference

### `featureFunction(options)`

**Parameters:**

| Name     | Type     | Required | Description              |
| -------- | -------- | -------- | ------------------------ |
| `param1` | `string` | Yes      | Description              |
| `param2` | `number` | No       | Description (default: 0) |

**Returns:** `ResultType`

**Throws:**

| Error Class       | Code              | When Raised                                   | Relevant Fields      |
| ----------------- | ----------------- | --------------------------------------------- | -------------------- |
| `ValidationError` | `INVALID_PARAM`   | Input fails schema validation                 | `field`, `expected`  |
| `TimeoutError`    | `TIMEOUT`         | Operation exceeds configured timeout          | `duration`, `limit`  |
| `NetworkError`    | `NETWORK_FAILURE` | Network request fails or is unreachable       | `url`, `statusCode`  |
| `NotFoundError`   | `NOT_FOUND`       | Requested resource does not exist             | `resourceId`, `type` |
| `ConflictError`   | `CONFLICT`        | State conflict (duplicate, concurrent update) | `conflictingId`      |

## Configuration

```typescript
// Configuration example if applicable
```

## Best Practices

- ✅ Do this
- ✅ Do that
- ❌ Don't do this

## Common Pitfalls

### Issue 1: {Description}

**Problem:**

```typescript
// Wrong way
```

**Solution:**

```typescript
// Right way
```

## Related

- [Other Feature](./other-feature.md)
- [API Reference](../api/reference.md)
