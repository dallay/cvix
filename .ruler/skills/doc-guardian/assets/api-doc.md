---
title: {Endpoint/API Name}
description: {What this API does}
last_updated: {YYYY-MM-DD}
---

# {API Name}

{Brief description}

## Endpoint

```markdown
{METHOD} /api/v1/{resource}
```

## Authentication

{Auth requirements - JWT, API key, etc.}

## Request

### Headers

| Header | Required | Description |
|--------|----------|-------------|
| `Authorization` | Yes | Bearer token |
| `Content-Type` | Yes | `application/json` |

### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `string` | Resource ID |

### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | `number` | No | Page number (default: 1) |
| `limit` | `number` | No | Items per page (default: 20) |

### Request Body

```typescript
interface RequestBody {
 field1: string;
 field2: number;
 field3?: boolean; // Optional
}
```

**Example:**

```json
{
  "field1": "value",
  "field2": 42,
  "field3": true
}
```

## Response

### Success (200 OK)

```typescript
interface SuccessResponse {
 data: ResourceType;
 meta: {
  timestamp: string;
 };
}
```

**Example:**

```json
{
  "data": {
    "id": "123",
    "name": "Example"
  },
  "meta": {
    "timestamp": "2026-01-11T10:00:00Z"
  }
}
```

### Error Responses

#### 400 Bad Request

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid field1",
    "details": {
      "field": "field1",
      "reason": "Must be non-empty string"
    }
  }
}
```

#### 401 Unauthorized

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid or missing token"
  }
}
```

#### 404 Not Found

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Resource not found"
  }
}
```

## Rate Limiting

- **Rate**: {requests} per {time period}
- **Header**: `X-RateLimit-Remaining`

## Code Examples

### TypeScript/JavaScript

```typescript
import { apiClient } from "@cvix/api";

const response = await apiClient.post("/api/v1/resource", {
 field1: "value",
 field2: 42,
});

console.log(response.data);
```

### cURL

```bash
curl -X POST https://api.cvix.dev/api/v1/resource \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "field1": "value",
    "field2": 42
  }'
```

### Kotlin

```kotlin
val response = client.post("/api/v1/resource") {
    contentType(ContentType.Application.Json)
    setBody(RequestBody(field1 = "value", field2 = 42))
}

println(response.body<SuccessResponse>())
```

## Implementation Details

{Link to backend implementation if relevant}

```kotlin
// Backend: server/engine/src/main/kotlin/com/cvix/api/ResourceController.kt
@RestController
@RequestMapping("/api/v1/resource")
class ResourceController(private val service: ResourceService) {
    @PostMapping
    fun create(@RequestBody body: RequestBody): ResponseEntity<SuccessResponse> {
        // Implementation
    }
}
```

## Related

- [Authentication Guide](../auth/overview.md)
- [Error Handling](../errors/overview.md)
- [Rate Limiting](../rate-limiting.md)
