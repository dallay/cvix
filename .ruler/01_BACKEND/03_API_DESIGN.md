# API Design Guidelines

> This guide covers the design of REST APIs, including URL structure, HTTP methods, status codes, and error handling.

## URL Structure

- Use nouns instead of verbs (e.g., `/users` instead of `/getUsers`).
- Use plural nouns for collections (e.g., `/users`, `/users/{userId}/posts`).
- Use kebab-case for URL segments.

## HTTP Methods

Use the appropriate HTTP method for the action:

- **`GET`**: Retrieve a resource or a collection of resources.
- **`POST`**: Create a new resource.
- **`PUT`**: Replace/update an existing resource.
- **`PATCH`**: Partially update an existing resource.
- **`DELETE`**: Delete a resource.

## Status Codes

Use standard HTTP status codes:

- **`200 OK`**: Successful request.
- **`201 Created`**: Resource was created successfully.
- **`204 No Content`**: Successful request with no response body (e.g., after a DELETE).
- **`400 Bad Request`**: Invalid request (e.g., validation error).
- **`401 Unauthorized`**: Authentication is required.
- **`403 Forbidden`**: Authenticated user is not authorized to perform the action.
- **`404 Not Found`**: The requested resource does not exist.
- **`500 Internal Server Error`**: A generic server-side error.

## Request and Response

- Use JSON for all request and response bodies.
- Use camelCase for all JSON property names.
- Version the API via the URL (e.g., `/api/users`).

## Error Handling

Use a consistent error format for all error responses:

```json
{
  "error": {
    "code": "invalid_request",
    "message": "The request was invalid.",
    "errors": [
      {
        "field": "email",
        "message": "The email address is invalid."
      }
    ]
  }
}
```

## Pagination, Sorting, and Filtering

- **Pagination**: Use query parameters like `page` and `size` to paginate collections.
- **Sorting**: Allow sorting via a `sort` parameter (e.g., `sort=name,asc`).
- **Filtering**: Allow filtering via query parameters (e.g., `status=active`).
