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

```typescript
// More complex usage with error handling, validation, and configuration
import { featureFunction, type FeatureConfig, FeatureError } from "@cvix/package";

const config: FeatureConfig = {
  param1: "value",
  param2: 42,
  enableValidation: true,
  timeout: 5000,
  retries: 3,
};

try {
  const result = await featureFunction(config);

  // Handle streaming results
  if (result.isStreaming) {
    for await (const chunk of result.stream()) {
      console.log("Received chunk:", chunk);
    }
  } else {
    console.log("Result:", result.data);
  }
} catch (error) {
  if (error instanceof FeatureError) {
    console.error(`Feature error [${error.code}]:`, error.message);
  } else {
    console.error("Unexpected error:", error);
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
- `ValidationError` — when input parameters fail validation (e.g., param1 is empty or param2 is negative)
- `TimeoutError` — when operation exceeds the configured timeout duration
- `NetworkError` — when network request fails or connection is lost
- `FeatureError` — when feature-specific operation fails (check error.code for details)

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

> **Note for template users:** Update these cross-document paths to match your repository structure. Use consistent relative patterns:
> - Same directory: `./sibling-file.md`
> - Parent directory: `../folder/file.md`
> - Multiple levels: `../../folder/subfolder/file.md`
> Verify all links resolve correctly from the document's location.
