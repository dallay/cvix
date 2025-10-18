# Docker Security Scanning Action

This action scans Docker images for vulnerabilities using Trivy and uploads SARIF reports to GitHub Security tab.

## Features

- Scans Docker images for HIGH and CRITICAL vulnerabilities
- Uploads SARIF reports to GitHub Security tab
- Generates artifacts with scan results
- Non-blocking by default (continues pipeline even if vulnerabilities are found)
- Configurable severity levels and failure behavior

## Usage

```yaml
- name: Scan Docker image for vulnerabilities
  uses: ./.github/actions/docker/security-scan
  with:
    image-ref: ${{ steps.build.outputs.image }}
    report-name: backend-security-scan
    category: backend-trivy
    severity: 'HIGH,CRITICAL'
    fail-on-error: 'false'
```

## Inputs

| Input | Description | Required | Default |
|-------|-------------|----------|---------|
| `image-ref` | Docker image reference to scan | Yes | - |
| `report-name` | Name for the security report file | Yes | - |
| `category` | SARIF category for GitHub Security tab | Yes | - |
| `severity` | Comma-separated list of severities to scan for | No | 'HIGH,CRITICAL' |
| `fail-on-error` | Whether to fail the pipeline if scan fails | No | 'false' |

## Outputs

| Output | Description |
|--------|-------------|
| `scan-result` | Result of the security scan (success, failed, error) |
| `sarif-file` | Path to the generated SARIF file |

## Integration with Docker Composition Actions

This security scanning action is integrated with all Docker composition actions:

1. **Backend Docker Action**: Scans Spring Boot images with category `backend-trivy`
2. **Frontend Web App Action**: Scans Vue.js web app images with category `frontend-web-trivy`
3. **Frontend Landing Page Action**: Scans Astro landing page images with category `frontend-landing-trivy`

The security scanning is configured to:

- Run for all Docker images, regardless of whether they are pushed to registries
- Not block the pipeline on scan failures (non-blocking)
- Generate artifacts with scan results for later analysis
- Upload SARIF reports to GitHub Security tab with appropriate categorization
