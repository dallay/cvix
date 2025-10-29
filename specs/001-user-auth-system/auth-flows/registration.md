# Registration Flow Diagram

This diagram illustrates the user registration process in the SaaS authentication system. The flow begins with a Vue.js registration form, which submits user data and a CSRF token to the Spring Boot backend. The backend validates the CSRF token and the input data. If validation passes, it attempts to create a new user in Keycloak. Success results in a 201 response and updates the frontend state, redirecting the user to the login page. Errors such as invalid data or existing users are handled with appropriate HTTP status codes and frontend error displays.

```mermaid
graph TD
A["Vue.js Form<br/>Register"] -->|POST /api/auth/register<br/>+ CSRF Token| B["Spring Boot Controller<br/>api/auth/register"]
B -->|Validates CSRF| C{CSRF<br/>Valid?}
C -->|No| D["❌ 403 Forbidden"]
C -->|Yes| E["Validates Data"]
E -->|Invalid| F["❌ 400 Bad Request"]
E -->|Valid| G["Keycloak<br/>Create User"]
G -->|User Created| H["✅ 201 Created<br/>User DTO"]
G -->|Error| I["❌ 409 Conflict<br/>User Exists"]
H -->|Axios Response| J["Pinia Store<br/>Update State"]
J -->|Redirect| K["Login Page"]
I -->|Axios Error| L["Show Error"]
```

