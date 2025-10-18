# Backend Docker Action

This action builds and pushes a Spring Boot Docker image using Gradle's `bootBuildImage` task.

## Key Features

- Uses existing Java setup action
- Executes `./gradlew bootBuildImage -x test`
- Supports both GHCR and Docker Hub publishing
- Includes Gradle dependency caching
- Integrated Trivy security scanning

## Usage Examples

### Build and Push Image (with delivery)

```yaml
- name: Build and push backend Docker image
  id: build-backend
  uses: ./.github/actions/docker/backend/action.yml
  with:
    image-name: backend
    github-token: ${{ secrets.GITHUB_TOKEN }}
    gradle-args: "-Pversion=${{ github.ref_name == 'main' && 'latest' || github.ref_name }} -Penv=production"
    module-path: server:engine
    deliver: 'true'  # Explicitly enable image pushing
```

### Build Image Only (no delivery)

```yaml
- name: Build backend Docker image without pushing
  id: build-backend
  uses: ./.github/actions/docker/backend/action.yml
  with:
    image-name: backend
    github-token: ${{ secrets.GITHUB_TOKEN }}
    gradle-args: "-Pversion=${{ github.ref_name == 'main' && 'latest' || github.ref_name }} -Penv=production"
    module-path: server:engine
    # deliver: 'false' is the default, so no need to specify it
```

## Inputs

| Input             | Description                        | Required | Default         |
| ----------------- | ---------------------------------- | -------- | --------------- |
| `image-name`      | Name of the Docker image           | Yes      | `backend`       |
| `github-token`    | GitHub token for authentication    | Yes      | -               |
| `docker-username` | Docker Hub username                | No       | -               |
| `docker-password` | Docker Hub password                | No       | -               |
| `deliver`         | Whether to push to registries      | No       | `false`         |
| `gradle-args`     | Additional Gradle arguments        | No       | -               |
| `module-path`     | Path to the Gradle module to build | No       | `server:engine` |
| `java-version`    | Java version to use                | No       | `21`            |

> **Note:** By default, if the `deliver` input is omitted, delivery is turned off by default (`false`). Specify `deliver: 'true'` to enable delivery. This ensures that omitting the key will not push images to registries.

## Outputs

| Output            | Description                                             |
| ----------------- | ------------------------------------------------------- |
| `image-full-name` | Full name of the built image including registry and tag |
| `image-tags`      | Tags applied to the image                               |

For more detailed documentation, see the [Docker Composition Actions Documentation](../../../../docs/workflows/docker-composition-actions.md#backend-docker-action).
