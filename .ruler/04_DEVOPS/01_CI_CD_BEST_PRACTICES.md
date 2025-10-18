# CI/CD Best Practices (GitHub Actions)

> This guide covers best practices for building robust, secure, and efficient CI/CD pipelines using GitHub Actions.

## Workflow Structure

- **Naming**: Use clear, descriptive names for workflow files (e.g., `backend-ci.yml`, `frontend-ci.yml`).
- **Triggers (`on`)**: Be specific with triggers. Use `push` and `pull_request` with branch and path filters to avoid unnecessary runs.
- **Concurrency**: Use `concurrency` groups to prevent race conditions on deployments or to cancel outdated builds on a branch.
- **Permissions**: Define `permissions` at the top level of the workflow to enforce the principle of least privilege for the `GITHUB_TOKEN`. Default to `contents: read`.

## Security

- **Secret Management**: Never hardcode secrets. Use encrypted GitHub Secrets (`${{ secrets.MY_SECRET }}`) and pass them to steps via environment variables.
- **OIDC**: Use OpenID Connect (OIDC) for authenticating with cloud providers (AWS, GCP, Azure) instead of storing long-lived credentials as secrets.
- **Dependency Scanning**: Integrate vulnerability scanning for dependencies (e.g., `dependency-review-action`, Snyk, Trivy) into the pipeline.
- **SAST**: Use Static Application Security Testing (SAST) tools like CodeQL to find vulnerabilities in your code.

## Optimization & Performance

- **Caching**: Use `actions/cache` to cache dependencies (`node_modules`, Gradle caches) and build outputs to speed up subsequent runs. Use `hashFiles` to create a reliable cache key.
- **Matrix Strategies**: Use `strategy: matrix` to run tests in parallel across different versions, operating systems, or other configurations.
- **Shallow Clones**: Use `actions/checkout@v4` with `fetch-depth: 1` for most CI jobs to avoid downloading the entire git history.

## Testing in CI

- **Unit & Integration Tests**: Run these on every push and pull request. They should be fast and reliable.
- **E2E Tests**: Run end-to-end tests against a staging environment, typically after a successful build and deployment to that environment.
- **Test Reporting**: Upload test results and code coverage reports as artifacts for analysis.

## Deployment

- **Environments**: Use GitHub Environments to protect important branches and environments (e.g., `production`). Enforce manual approvals for production deployments.
- **Rollbacks**: Have a clear, tested rollback strategy. This could be a manual process or an automated workflow triggered on failure.
