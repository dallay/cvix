# API Design Guidelines

> Conventions for designing REST APIs, including URL structure, HTTP methods, status codes, and error handling.

## URL Structure

- Use **nouns** instead of verbs (e.g., `/users` instead of `/getUsers`)
- Use **plural nouns** for collections (e.g., `/users`, `/users/{userId}/posts`)
- Use **kebab-case** for URL segments (e.g., `/user-profiles`)

## HTTP Methods

| Method  | Purpose                          | Example                      |
|--------|-----------------------------------|------------------------------|
| `GET`     | Retrieve a resource or collection   | `GET /users`, `GET /users/{id}`   |
| `POST`    | Create a new resource              | `POST /users`                     |
| `PUT`     | Replace/update an existing resource| `PUT /users/{id}`                 |
| `PATCH`   | Partially update an existing resource | `PATCH /users/{id}`              |
| `DELETE`  | Delete a resource                  | `DELETE /users/{id}`              |


## Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| `200 OK` | Successful request | General success |
| `201 Created` | Resource was created successfully | After POST |
| `204 No Content` | Successful request with no response body | After DELETE |
| `400 Bad Request` | Invalid request | Validation error |
| `401 Unauthorized` | Authentication is required | Missing/invalid token |
| `403 Forbidden` | Authenticated but not authorized | Insufficient permissions |
| `404 Not Found` | Resource does not exist | Invalid ID |
| `409 Conflict` | State conflict | Duplicate resource |
| `422 Unprocessable Entity` | Semantic validation error | Business rule violation |
| `429 Too Many Requests` | Rate limit exceeded | Throttling |
| `500 Internal Server Error` | Generic server-side error | Unexpected failure |

## Request and Response

- Use **JSON** for all request and response bodies
- Use **camelCase** for all JSON property names
- Version the API via header with `Accept` and `Content-Type` (e.g., `application/vnd.api.v1+json`)

## Error Handling

Use a consistent error format for all error responses (RFC 7807 Problem Details):

```json
{
  "type": "https://example.com/problems/invalid-request",
  "title": "Invalid Request",
  "status": 400,
  "detail": "The request was invalid.",
  "instance": "/api/users",
  "errors": [
    {
      "field": "email",
      "message": "The email address is invalid."
    }
  ]
}
```

## Pagination

- Use query parameters: `page` (0-indexed), `size`, `sort`
- Return pagination metadata in responses:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

- Support cursor-based pagination for large datasets or real-time feeds
- Document pagination parameters in OpenAPI/Swagger

## Sorting

- Allow sorting via a `sort` parameter
- Format: `sort=field,direction` (e.g., `sort=name,asc`)
- Support multiple sort fields: `sort=lastName,asc&sort=firstName,asc`

## Filtering

- Allow filtering via query parameters (e.g., `status=active`, `createdAfter=2024-01-01`)
- Use consistent naming conventions for filter parameters
- Document available filters in OpenAPI/Swagger
