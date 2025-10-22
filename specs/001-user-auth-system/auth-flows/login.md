# Login Flow Diagram

This diagram describes the user login process in the SaaS authentication system. The flow starts with the Vue.js login form, which sends credentials and a CSRF token to the Spring Boot backend. The backend validates the CSRF token and credentials, then delegates authentication to Keycloak. If authentication succeeds, the backend issues a JWT and returns a 200 response. The frontend updates the Pinia store and redirects the user to the dashboard. Errors such as invalid credentials or CSRF failures are handled with appropriate status codes and error messages.

```mermaid
graph TD
A["Vue.js Form<br/>Login"] -->|POST /api/auth/login<br/>+ CSRF Token| B["Spring Boot Controller<br/>api/auth/login"]
B -->|Validates CSRF| C{CSRF<br/>Valid?}
C -->|No| D["❌ 403 Forbidden"]
C -->|Yes| E["Validates Credentials"]
E -->|Invalid| F["❌ 401 Unauthorized"]
E -->|Valid| G["Keycloak<br/>Authenticate"]
G -->|Success| H["✅ 200 OK<br/>JWT"]
G -->|Error| I["❌ 401 Unauthorized"]
H -->|Axios Response| J["Pinia Store<br/>Update State"]
J -->|Redirect| K["Dashboard"]
I -->|Axios Error| L["Show Error"]
```

