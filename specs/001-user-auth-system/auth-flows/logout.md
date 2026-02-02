# Logout Flow Diagram

This diagram explains the user logout process in the SaaS authentication system. The flow starts when the user triggers a logout action in the frontend, which sends a POST request to the Spring Boot backend. The backend invalidates the user's session or JWT, optionally notifies Keycloak to revoke tokens, and returns a 204 No Content response. The frontend clears the Pinia store and redirects the user to the login page. Any errors are handled with appropriate status codes and error messages.

```mermaid
graph TD
A["Vue.js<br/>Logout Action"] -->|POST /api/auth/logout| B["Spring Boot Controller<br/>api/auth/logout"]
B -->|Invalidate Session/JWT| C["Keycloak<br/>Revoke Token (optional)"]
C -->|Success| D["✅ 204 No Content"]
C -->|Error| E["❌ 400 Bad Request"]
D -->|Axios Response| F["Pinia Store<br/>Clear State"]
F -->|Redirect| G["Login Page"]
E -->|Axios Error| H["Show Error"]
```
