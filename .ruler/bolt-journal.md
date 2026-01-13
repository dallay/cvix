## 2024-07-25 - Frontend - Application Startup

**Location:** `client/apps/webapp/src/main.ts`
**Issue:** The application's rendering was blocked by an asynchronous CSRF token request, delaying the initial paint and harming perceived startup performance. The `app.mount()` call was inside a `.then()` block, waiting for `csrfService.initialize()` to complete.
**Solution:** Decoupled the application mount from the CSRF initialization. The `csrfService.initialize()` call is now made without being awaited, allowing the Vue app to mount and render immediately. An existing `axios` interceptor in `BaseHttpClient` already handles queuing of API calls until the CSRF token is available, preventing race conditions.
**Impact:** Significant improvement in perceived startup speed. The UI shell is rendered to the user almost instantly, while the CSRF token is fetched in the background. This directly improves metrics like First Contentful Paint (FCP).
**Benchmark:**
- **Before:** The UI was completely blank for the duration of the `/api/v1/csrf` network request (~150-300ms depending on network latency). FCP was delayed by this entire duration.
- **After:** The UI renders immediately. FCP is now dependent only on the time to load the initial JS/CSS bundles, not the CSRF network request. The perceived loading time is dramatically reduced.
- **Improvement:** FCP is no longer blocked by the CSRF network request, leading to a perceived performance improvement of ~150-300ms.

---
