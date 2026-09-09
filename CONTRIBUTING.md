# Contributing to NovaFetch

Thanks for helping improve NovaFetch! Contributions are welcome through issues, pull requests, documentation, testing, and design feedback.

## Before you start

- Read the [README](README.md) and project architecture notes.
- Search existing issues and pull requests before opening a duplicate.
- For security vulnerabilities, do **not** open a public issue. Follow [SECURITY.md](SECURITY.md).

## Development setup

Requirements:

- Android Studio Ladybug or newer
- JDK 21
- Android SDK 35
- Git

Build locally with:

```bash
./gradlew clean
./gradlew assembleDebug
```

For a release-style verification:

```bash
./gradlew assembleRelease
```

## Pull requests

1. Create a focused branch from the current development branch.
2. Keep changes small and logically grouped.
3. Use clear commit messages.
4. Add or update tests when behavior changes.
5. Check Compose layouts on small and large screens.
6. Run the relevant Gradle checks before opening the PR.
7. Update documentation when public behavior, setup, or architecture changes.

## Commit style

Prefer Conventional Commit-style messages:

- `feat:` new functionality
- `fix:` bug fix
- `refactor:` internal restructuring
- `ui:` visual/UX changes
- `docs:` documentation
- `build:` build/dependency changes
- `test:` tests
- `chore:` maintenance

Example:

```text
fix: prevent duplicate downloads from queue
```

## Code quality

- Follow Kotlin and Android conventions.
- Prefer immutable state and unidirectional data flow in Compose.
- Avoid hard-coded secrets, tokens, credentials, or private endpoints.
- Keep download-engine behavior isolated from UI code.
- Do not introduce a dependency without a clear reason and compatible license.
- Preserve accessibility: content descriptions, readable contrast, touch targets, and dynamic text.

## UI contributions

NovaFetch uses a lightweight liquid-glass visual language based on the Nova aqua palette. New UI should remain consistent with the existing spacing, typography, shapes, motion, light/dark themes, and responsive behavior.

## Review checklist

- [ ] Builds successfully with JDK 21.
- [ ] Debug APK builds.
- [ ] Release APK builds when applicable.
- [ ] No new warnings that indicate a correctness problem.
- [ ] No secrets or credentials added.
- [ ] No unnecessary dependency added.
- [ ] UI has been checked for overflow and accessibility.
- [ ] Documentation updated when needed.
