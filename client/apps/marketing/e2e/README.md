# Marketing Site E2E Tests

End-to-end tests for the marketing site contact form using Playwright.

## Setup

### Install Playwright Browsers (First Time Only)

```bash
cd client/apps/marketing
pnpm run playwright:install
```

This installs Chromium, Firefox, and WebKit browsers along with their system dependencies.

## Running Tests

### Prerequisites

The E2E tests can run in two modes:

#### 1. Live Backend Integration (Default)

Requires the Spring Boot backend to be running:

```bash
# In terminal 1: Start backend
./gradlew :server:engine:bootRun

# In terminal 2: Run E2E tests
cd client/apps/marketing
pnpm run test:e2e
```

The Playwright config will automatically:
- Start the marketing site dev server on port 7766
- Proxy API calls to `http://localhost:8080`
- Run tests against the live backend

#### 2. HAR Replay Mode (CI/CD)

In CI or when `USE_HAR=true`, tests replay pre-recorded API responses from HAR files:

```bash
USE_HAR=true pnpm run test:e2e
```

> **Note:** HAR files are not yet recorded. To record HAR files, see the "Recording HAR Files" section below.

### Test Commands

```bash
# Run all tests headlessly (default)
pnpm run test:e2e

# Run tests with Playwright UI (interactive)
pnpm run test:e2e:ui

# Run tests in headed mode (see browser)
pnpm run test:e2e:headed

# Debug tests step-by-step
pnpm run test:e2e:debug

# Run specific test file
pnpm run test:e2e e2e/contact-form.spec.ts

# Run tests in a single browser
pnpm run test:e2e --project=chromium
```

## Test Coverage

### Contact Form Tests (`e2e/contact-form.spec.ts`)

The test suite covers:

1. **Form Rendering**
   - All form fields are visible (name, email, subject, message)
   - Submit button is present and enabled

2. **Browser Validation**
   - Required fields trigger browser validation
   - Invalid email format is caught by browser

3. **CSRF Token Lazy Loading**
   - CSRF token is not present initially
   - CSRF token loads on first field interaction

4. **Form Submission (Live Backend Required)**
   - Valid form submission succeeds
   - Success message is displayed
   - Form is reset after successful submission

5. **Internationalization (i18n)**
   - Form renders correctly in English (`/en/contact`)
   - Form renders correctly in Spanish (`/es/contact`)

6. **Error Handling**
   - Network errors display error message
   - Error message has appropriate styling

7. **Visual Regression**
   - Screenshot of initial form state for comparison

### Test Organization

```text
client/apps/marketing/
├── e2e/
│   └── contact-form.spec.ts    # Contact form E2E tests
├── playwright.config.ts         # Playwright configuration
├── playwright-report/           # Test reports (generated, gitignored)
└── test-results/                # Test artifacts (generated, gitignored)
```

## Configuration

### Playwright Config (`playwright.config.ts`)

Key settings:

| Setting              | Value                          | Purpose                                    |
|----------------------|--------------------------------|--------------------------------------------|
| Base URL             | `http://localhost:7766`        | Marketing site dev server (HTTP mode)      |
| Backend URL          | `http://localhost:8080`        | Backend API for contact form               |
| Timeout              | 30s navigation, 15s action     | Account for dev server startup             |
| Retries              | 2 in CI, 1 locally             | Flake resistance                           |
| Workers              | 2 in CI, unlimited locally     | Resource management                        |
| Browsers             | Chromium, Firefox, WebKit      | Cross-browser compatibility                |
| Screenshots          | On failure in CI               | Debugging failed tests                     |
| Trace                | On first retry in CI           | Detailed debugging info                    |

### Environment Variables

| Variable                  | Default                   | Purpose                                    |
|---------------------------|---------------------------|--------------------------------------------|
| `CVIX_API_URL`            | `https://localhost:8443`  | Backend API URL for the app                |
| `PLAYWRIGHT_BACKEND_URL`  | `http://localhost:8080`   | Override backend URL for Playwright tests  |
| `USE_HAR`                 | `false` (CI: `true`)      | Enable HAR replay mode                     |
| `RECORD_HAR`              | `false`                   | Record new HAR files                       |
| `FORCE_HTTP`              | Auto-detected             | Force HTTP instead of HTTPS                |

## Recording HAR Files

To record HAR files for offline testing or CI:

### Option 1: Using Playwright Codegen

```bash
# Start backend
./gradlew :server:engine:bootRun

# In another terminal, record HAR
cd client/apps/marketing
npx playwright codegen \
  --save-har=e2e/hars/contact-form.har \
  http://localhost:7766/en/contact
```

Then interact with the form in the browser that opens. Playwright will record all network traffic to the HAR file.

### Option 2: Using Test Environment Variables

```bash
# Start backend
./gradlew :server:engine:bootRun

# Run tests in recording mode
cd client/apps/marketing
RECORD_HAR=true pnpm run test:e2e
```

HAR files will be saved in `e2e/hars/` directory.

## Troubleshooting

### Port 7766 Already in Use

```bash
# Find and kill process using port 7766
lsof -ti :7766 | xargs kill -9

# Or use a different port
PORT=7777 pnpm run test:e2e
```

### Backend Connection Refused

Ensure the backend is running:

```bash
./gradlew :server:engine:bootRun
```

Or skip backend tests:

```bash
USE_HAR=true pnpm run test:e2e
```

### SSL Certificate Errors

The tests use HTTP by default to avoid SSL issues. If you see SSL errors:

```bash
FORCE_HTTP=true pnpm run test:e2e
```

### Tests Timeout During Dev Server Startup

The dev server can take up to 5 minutes to start (includes dependency builds). If it times out:

1. Start the dev server manually first:
   ```bash
   pnpm run dev
   ```

2. Then run tests with reuse flag:
   ```bash
   pnpm run test:e2e
   ```

## CI/CD Integration

Tests run automatically in GitHub Actions on:
- Pull requests
- Pushes to main branch

CI configuration uses:
- HAR replay mode (`USE_HAR=true`)
- 2 parallel workers
- Retry on failure (up to 2 retries)
- HTML, JSON, and JUnit reports
- Screenshots on failure
- Trace on first retry

## Future Improvements

- [ ] Record HAR files for offline testing
- [ ] Add visual regression testing with screenshot comparison
- [ ] Test hCaptcha integration (currently skipped)
- [ ] Add mobile viewport tests
- [ ] Add accessibility (a11y) audits with axe-core
- [ ] Add performance testing (Lighthouse)
