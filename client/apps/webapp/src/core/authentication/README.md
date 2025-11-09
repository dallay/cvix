# Authentication & Authorization

Este documento explica cómo funciona el sistema de autenticación y autorización del frontend.

## 📋 Características Implementadas

- ✅ **Autenticación completa** (login, registro, logout, refresh token)
- ✅ **Protección de rutas** con navigation guards
- ✅ **Control de acceso basado en roles (RBAC)**
- ✅ **Redirección inteligente** después del login
- ✅ **Remember Me** con cookies de larga duración
- ✅ **Composables y directivas** para facilitar verificaciones de permisos

## 🔐 Roles Disponibles

Los roles vienen desde el backend (Keycloak):

- `ROLE_ADMIN` - Administrador con acceso completo
- `ROLE_USER` - Usuario regular
- `ROLE_ANONYMOUS` - Usuario no autenticado

## 🛣️ Protección de Rutas

### Configuración Básica

En `index.ts`, define los meta campos para cada ruta:

```typescript
{
  path: "/dashboard",
  name: "Dashboard",
  component: () => import("./pages/DashboardPage.vue"),
  meta: {
    requiresAuth: true,        // Requiere estar autenticado
    title: "Dashboard",
  },
}
```

### Protección por Roles

Para restringir acceso por roles:

```typescript
{
  path: "/admin",
  name: "AdminPanel",
  component: () => import("./pages/AdminPage.vue"),
  meta: {
    requiresAuth: true,
    roles: ["ROLE_ADMIN"],     // Solo admins pueden acceder
    title: "Admin Panel",
  },
}
```

Múltiples roles (el usuario necesita tener AL MENOS UNO):

```typescript
{
  path: "/moderation",
  name: "Moderation",
  component: () => import("./pages/ModerationPage.vue"),
  meta: {
    requiresAuth: true,
    roles: ["ROLE_ADMIN", "ROLE_MODERATOR"],  // Admin O Moderador
    title: "Moderation",
  },
}
```

### Rutas Solo para Invitados

Para páginas que solo usuarios no autenticados pueden ver (login, registro):

```typescript
{
  path: "/login",
  name: "Login",
  component: () => import("./pages/LoginPage.vue"),
  meta: {
    requiresGuest: true,       // Solo usuarios no autenticados
    title: "Login",
  },
}
```

## 🎯 Uso en Componentes

### 1. Composable `useAuth()`

Para verificar permisos dentro de componentes:

```vue
<script setup lang="ts">
import { useAuth } from "@/authentication/presentation/composables/useAuth";

const { user, isAuthenticated, hasRole, hasAnyRole, hasAllRoles, isAdmin, isUser } = useAuth();

// Verificar un rol específico
if (hasRole("ROLE_ADMIN")) {
  // Lógica de admin
}

// Verificar si tiene alguno de los roles
if (hasAnyRole(["ROLE_ADMIN", "ROLE_MODERATOR"])) {
  // Mostrar contenido de moderación
}

// Verificar si tiene todos los roles
if (hasAllRoles(["ROLE_USER", "ROLE_PREMIUM"])) {
  // Mostrar contenido premium
}
</script>

<template>
  <div>
    <h1>Welcome {{ user?.firstName }}</h1>

    <!-- Mostrar solo si está autenticado -->
    <div v-if="isAuthenticated">
      <p>You are logged in!</p>
    </div>

    <!-- Mostrar solo si es admin -->
    <button v-if="isAdmin">
      Admin Settings
    </button>
  </div>
</template>
```

### 2. Directiva `v-role`

Para mostrar/ocultar elementos según roles:

```vue
<template>
  <div>
    <!-- Mostrar solo si el usuario tiene ROLE_ADMIN -->
    <button v-role="'ROLE_ADMIN'">
      Delete User
    </button>

    <!-- Mostrar si tiene cualquiera de los roles -->
    <button v-role="{ roles: ['ROLE_ADMIN', 'ROLE_MODERATOR'], mode: 'any' }">
      Moderate Content
    </button>

    <!-- Mostrar solo si tiene TODOS los roles -->
    <div v-role="{ roles: ['ROLE_USER', 'ROLE_PREMIUM'], mode: 'all' }">
      Premium Content
    </div>
  </div>
</template>
```

**Nota:** Para usar la directiva, debes registrarla en `main.ts`:

```typescript
import { vRole } from "@/authentication/presentation/directives/role.directive";

app.directive("role", vRole);
```

## 🔄 Flujo de Autenticación

### 1. Login

```typescript
import { useAuthStore } from "@/authentication/presentation/stores/authStore";

const authStore = useAuthStore();

await authStore.login({
  email: "user@example.com",
  password: "gp1O_Kj_3x0XybL*123",
  rememberMe: true  // Cookies de 30 días, false = 1 día
});

// Después del login exitoso, el usuario es redirigido automáticamente
```

### 2. Verificación en Ruta Protegida

Cuando el usuario intenta acceder a una ruta protegida:

```text
1. Navigation guard verifica si requiresAuth: true
2. Si no está autenticado, llama a authStore.checkAuth()
3. Si checkAuth() falla, redirige a /login?redirect=/dashboard
4. Después del login, redirige automáticamente a /dashboard
```

### 3. Verificación de Roles

Si una ruta tiene `meta.roles`:

```text
1. Navigation guard verifica si el usuario tiene al menos uno de los roles requeridos
2. Si NO tiene el rol, redirige a /unauthorized?from=/admin
3. Si tiene el rol, permite el acceso
```

## 🚀 Redirección Inteligente

### Después del Login

Si el usuario intenta acceder a una ruta protegida sin estar autenticado:

```text
1. Usuario va a /dashboard (sin estar autenticado)
2. Guard redirige a /login?redirect=/dashboard
3. Usuario hace login
4. Sistema lee el query param "redirect" y redirige a /dashboard
```

### Usuarios ya Autenticados

Si un usuario autenticado intenta acceder a rutas de invitado (login, register):

```text
1. Usuario autenticado va a /login
2. Guard detecta requiresGuest: true
3. Lee el query param "redirect" o usa "/dashboard" por defecto
4. Redirige automáticamente
```

## 📝 Ejemplos Completos

### Página con Contenido Condicional

```vue
<script setup lang="ts">
import { useAuth } from "@/authentication/presentation/composables/useAuth";

const { user, isAuthenticated, isAdmin, hasAnyRole } = useAuth();
</script>

<template>
  <div class="dashboard">
    <h1>Dashboard</h1>

    <!-- Siempre visible para usuarios autenticados -->
    <div class="user-info">
      <p>Welcome, {{ user?.firstName }} {{ user?.lastName }}</p>
      <p>Email: {{ user?.email }}</p>
    </div>

    <!-- Solo para admins -->
    <section v-if="isAdmin" class="admin-section">
      <h2>Admin Tools</h2>
      <button>Manage Users</button>
      <button>View Reports</button>
    </section>

    <!-- Para admins O moderadores -->
    <section v-if="hasAnyRole(['ROLE_ADMIN', 'ROLE_MODERATOR'])">
      <h2>Moderation Tools</h2>
      <button>Review Content</button>
    </section>

    <!-- Usando directiva v-role -->
    <button v-role="'ROLE_ADMIN'" class="danger">
      Delete All Data
    </button>
  </div>
</template>
```

### Navegación Programática con Verificación

```typescript
import { useRouter } from "vue-router";
import { useAuth } from "@/authentication/presentation/composables/useAuth";

const router = useRouter();
const { hasRole } = useAuth();

function goToAdminPanel() {
  if (hasRole("ROLE_ADMIN")) {
    router.push("/admin");
  } else {
    // Mostrar mensaje o redirigir
    router.push("/unauthorized");
  }
}
```

## 🛡️ Seguridad

### Importante

- ⚠️ **Nunca confíes solo en la verificación del frontend**
- ⚠️ El backend SIEMPRE debe verificar permisos
- ⚠️ El frontend solo oculta UI, no protege datos

### Mejores Prácticas

1. **Siempre verifica permisos en el backend**
2. **Usa navigation guards para UX**, no para seguridad
3. **Oculta elementos de UI** para evitar confusión
4. **Maneja errores 401/403** del backend apropiadamente

## 📚 Tipos TypeScript

Los tipos están definidos en `router/types.d.ts`:

```typescript
interface RouteMeta {
  requiresAuth?: boolean;
  requiresGuest?: boolean;
  roles?: string | string[];
  title?: string;
}
```

## 🔧 Configuración

### Registrar la Directiva (main.ts)

```typescript
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import { vRole } from "@/authentication/presentation/directives/role.directive";

const app = createApp(App);

app.use(router);
app.directive("role", vRole);

app.mount("#app");
```

### Inicializar Auth Store (main.ts)

```typescript
import { useAuthStore } from "@/authentication/presentation/stores/authStore";

const app = createApp(App);
// ... configuración

app.mount("#app");

// Inicializar auth después de montar
const authStore = useAuthStore();
authStore.initialize();
```

## 📖 Referencias

- [Vue Router Navigation Guards](https://router.vuejs.org/guide/advanced/navigation-guards.html)
- [Pinia Store](https://pinia.vuejs.org/)
- [Keycloak Roles](https://www.keycloak.org/docs/latest/server_admin/#_roles)
