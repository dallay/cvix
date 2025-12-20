# Playwright E2E Test Action

Reusable GitHub Action for running Playwright E2E tests with intelligent browser caching and comprehensive reporting.

## Features

- ✅ **Smart Browser Caching**: Caches Playwright browsers by version to speed up CI runs
- 🎭 **Multi-Browser Support**: Test against Chromium, Firefox, WebKit, or all browsers
- 📊 **Comprehensive Reporting**: Generates HTML reports, JUnit XML, and JSON test results
- 🔄 **Automatic Server Management**: Starts and stops the dev server automatically
- 📦 **Artifact Upload**: Automatically uploads test reports and traces on failure
- 🎯 **Parallel Execution**: Supports matrix strategy for testing multiple browsers in parallel

## Usage

### Basic Usage

```yaml
- name: Run E2E tests
  uses: ./.github/actions/test-playwright
  with:
    browser: chromium
```

### Full Example with Matrix Strategy

```yaml
e2e-tests:
  name: E2E Tests
  runs-on: ubuntu-latest
  strategy:
    fail-fast: false
    matrix:
      browser: [chromium, firefox, webkit]
  steps:
    - name: Checkout code
      uses: actions/checkout@8e8c483db84b4bee98b60c0593521ed34d9990e8    # v6

    - name: Setup Node.js
      uses: ./.github/actions/setup/node
      with:
        node-version-file: '.nvmrc'

    - name: Build dependencies (recommended for faster startup)
      run: pnpm --filter @cvix/utilities build

    - name: Run E2E tests
      uses: ./.github/actions/test-playwright
      with:
        browser: ${{ matrix.browser }}
        upload-results: 'true'
        working-directory: 'client/apps/webapp'
```

## Inputs

| Input               | Description                                      | Required | Default             |
| ------------------- | ------------------------------------------------ | -------- | ------------------- |
| `browser`           | Browser to test (chromium/firefox/webkit/all)    | No       | `chromium`          |
| `headed`            | Run in headed mode (visible browser)             | No       | `false`             |
| `upload-results`    | Upload test results and traces                   | No       | `true`              |
| `working-directory` | Working directory for tests                      | No       | `client/apps/webapp` |

## Outputs

| Output         | Description                     |
| -------------- | ------------------------------- |
| `tests-status` | Test execution status (success/failed) |
| `report-path`  | Path to the generated test report     |

## How It Works

1. **Version Detection**: Detects the installed Playwright version using `pnpm list @playwright/test --json`
2. **Browser Caching**: Caches browsers based on OS + Playwright version + browser type
3. **Conditional Installation**:
   - If cache hit: Only installs system dependencies
   - If cache miss: Installs browsers and system dependencies
4. **Test Execution**: Runs Playwright tests with the specified browser
5. **Artifact Upload**: Uploads reports and traces (on failure) for debugging

## Browser-Specific Notes

### Chromium
- Fastest execution time
- Best for quick feedback loops
- Recommended for PR checks

### Firefox
- Good cross-browser compatibility testing
- Slightly slower than Chromium

### WebKit
- Emulates Safari behavior
- Includes 50ms slowdown for stability
- Longest execution time
- Essential for iOS/macOS compatibility

## Cache Strategy

The action uses a hierarchical cache key strategy:

```text
${{ runner.os }}-playwright-${{ version }}-${{ browser }}
```

With fallback keys:

```text
${{ runner.os }}-playwright-${{ version }}-
${{ runner.os }}-playwright-
```

This ensures:
- ✅ Same browser reuses exact cache
- ✅ Different browsers share base Playwright installation
- ✅ Version upgrades invalidate cache appropriately

## Troubleshooting

### Tests Failing in CI but Passing Locally

1. Check browser-specific issues (especially WebKit)
2. Review uploaded traces in artifacts
3. Enable headed mode temporarily for debugging:
   ```yaml
   with:
     headed: 'true'
   ```

### Cache Not Working

1. Verify Playwright version is consistent across runs
2. Check cache size limits (GitHub has 10GB per repository)
3. Manually clear cache if corrupted:
   - Go to Actions → Caches → Delete specific cache

### Server Not Starting

1. Check `webServer` configuration in `playwright.config.ts`
2. Verify port availability (default: 9876)
3. Check server startup logs in CI output
4. Increase `webServer.timeout` if needed

## Related Files

- **Playwright Config**: `client/apps/webapp/playwright.config.ts`
- **Test Files**: `client/apps/webapp/e2e/*.spec.ts`
- **CI Workflow**: `.github/workflows/ci.yml`

## References

- [Playwright Documentation](https://playwright.dev/)
- [GitHub Actions Cache](https://docs.github.com/en/actions/using-workflows/caching-dependencies-to-speed-up-workflows)
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
